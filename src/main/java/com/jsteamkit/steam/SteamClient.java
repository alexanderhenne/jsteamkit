package com.jsteamkit.steam;

import com.google.common.base.Throwables;
import com.google.protobuf.ByteString;
import com.jsteamkit.cm.CMServer;
import com.jsteamkit.cm.CMServerList;
import com.jsteamkit.event.EventHandler;
import com.jsteamkit.event.EventListener;
import com.jsteamkit.internals.proto.SteammessagesBase;
import com.jsteamkit.internals.proto.SteammessagesClientserver;
import com.jsteamkit.internals.proto.SteammessagesClientserver2;
import com.jsteamkit.internals.steamlanguage.EAccountType;
import com.jsteamkit.internals.steamlanguage.EMsg;
import com.jsteamkit.internals.steamlanguage.EResult;
import com.jsteamkit.internals.steamlanguage.EUniverse;
import com.jsteamkit.internals.steamlanguageinternal.MsgProtoBuf;
import com.jsteamkit.internals.steamlanguageinternal.Msg;
import com.jsteamkit.internals.steamlanguageinternal.MsgHeader;
import com.jsteamkit.internals.steamlanguageinternal.MsgHeaderProtoBuf;
import com.jsteamkit.internals.stream.BinaryReader;
import com.jsteamkit.internals.messages.MsgChannelEncryptRequest;
import com.jsteamkit.internals.messages.MsgChannelEncryptResponse;
import com.jsteamkit.internals.messages.MsgChannelEncryptResult;
import com.jsteamkit.internals.net.TcpConnection;
import com.jsteamkit.util.CryptoUtil;
import com.jsteamkit.util.PublicKeys;
import com.jsteamkit.util.ZipUtil;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class SteamClient {

    public CMServer cmServer = CMServerList.getBestServer();
    public TcpConnection connection;

    private Map<EMsg, EventListener> eventListeners = new HashMap<>();

    public EUniverse connectedUniverse = EUniverse.Invalid;
    public long steamId;

    public SteamClient() {
        this.registerEventHandler(EMsg.ChannelEncryptRequest, (d) -> {
            MsgChannelEncryptRequest body = new MsgChannelEncryptRequest();
            try {
                new Msg(new MsgHeader(), body).decode(d);
            } catch (IOException e) {
                Throwables.propagate(e);
            }

            System.out.println(MessageFormat.format("Received encryption request. Universe: {0}, Protocol version: {1}",
                    body.universe,
                    body.protocolVersion));

            BigInteger[] keys = PublicKeys.keys.get(connectedUniverse = body.universe);
            if (keys != null) {
                connection.sessionKey = CryptoUtil.getRandomBytes(32);
                try {
                    MsgHeader replyHeader = new MsgHeader();
                    replyHeader.msg = EMsg.ChannelEncryptResponse;
                    MsgChannelEncryptResponse replyBody = new MsgChannelEncryptResponse();
                    Msg replyMsg = new Msg(replyHeader, replyBody);

                    byte[] encryptedSessionKey = CryptoUtil.encryptWithRsa(connection.sessionKey, keys[0], keys[1]);
                    byte[] sessionKeyCrc = CryptoUtil.getCrcHash(encryptedSessionKey);

                    replyMsg.writer.write(encryptedSessionKey);
                    replyMsg.writer.write(sessionKeyCrc);
                    replyMsg.writer.write(0);

                    byte[] encodedMsg = replyMsg.encode();
                    connection.sendPacket(encodedMsg);
                } catch (IOException | GeneralSecurityException e) {
                    Throwables.propagate(e);
                }
            } else {
                System.out.println("Universe " + body.universe + " is not supported!");
            }
        });

        this.registerEventHandler(EMsg.ChannelEncryptResult, (d) -> {
            MsgChannelEncryptResult body = new MsgChannelEncryptResult();
            try {
                new Msg(new MsgHeader(), body).decode(d);
            } catch (IOException e) {
                Throwables.propagate(e);
            }

            System.out.println(MessageFormat.format("Received encryption result. Result: {0}",
                    body.result));

            if (body.result == EResult.OK) {
                connection.encrypted = true;

                System.out.println("Successfully connected to Steam. You can now attempt to login.");
            } else {
                System.out.println(MessageFormat.format("Encryption failed. Result: {0}", body.result));
            }
        });

        this.registerEventHandler(EMsg.Multi, (d) -> {
            MsgHeaderProtoBuf header = new MsgHeaderProtoBuf();
            SteammessagesBase.CMsgMulti.Builder body
                    = SteammessagesBase.CMsgMulti.newBuilder();
            try {
                new MsgProtoBuf(header, body).decode(d);
            } catch (IOException e) {
                Throwables.propagate(e);
            }

            byte[] payload = body.getMessageBody().toByteArray();

            if (body.getSizeUnzipped() > 0) {
                try {
                    payload = ZipUtil.unzip(payload);
                } catch (IOException e) {
                    Throwables.propagate(e);
                }
            }

            BinaryReader reader = new BinaryReader(payload);
            try {
                while (!reader.reader.isAtEnd()) {
                    int size = reader.readInt();
                    byte[] data = reader.readBytes(size);
                    connection.handleMsg(data);
                }
            } catch (IOException e) {
                Throwables.propagate(e);
            }
        });

        this.registerEventHandler(EMsg.ClientLogOnResponse, (d) -> {
            MsgHeaderProtoBuf header = new MsgHeaderProtoBuf();
            SteammessagesClientserver.CMsgClientLogonResponse.Builder body
                    = SteammessagesClientserver.CMsgClientLogonResponse.newBuilder();
            try {
                new MsgProtoBuf(header, body).decode(d);
            } catch (IOException e) {
                Throwables.propagate(e);
            }

            EResult result = EResult.get(body.getEresult());
            if (result == EResult.OK) {
                steamId = header.proto.getSteamid();

                System.out.println("Successfully logged in. Steam ID: " + steamId);
            } else {
                System.out.println("Failed login. Result: " + result);
            }
        });

        this.registerEventHandler(EMsg.ClientLoggedOff, (d) -> {
            MsgHeaderProtoBuf header = new MsgHeaderProtoBuf();
            SteammessagesClientserver.CMsgClientLoggedOff.Builder body
                    = SteammessagesClientserver.CMsgClientLoggedOff.newBuilder();
            try {
                new MsgProtoBuf(header, body).decode(d);
            } catch (IOException e) {
                Throwables.propagate(e);
            }

            System.out.println("Logged out of Steam. Result: " + EResult.get(body.getEresult()));
        });
    }

    public void connect(boolean verbose) throws IOException {
        connection = new TcpConnection(verbose) {
            @Override
            public void handleEvent(EMsg eventMsg, byte[] data) {
                EventListener eventListener = eventListeners.get(eventMsg);
                if (eventListener != null) {
                    eventListener.runHandlers(data);
                }
            }
        };
        connection.connect(cmServer);
    }

    public void login(LoginCredentials credentials) {
        if (credentials.username.length() != 0
                && credentials.password.length() != 0) {
            try {
                SteammessagesBase.CMsgProtoBufHeader.Builder proto
                        = SteammessagesBase.CMsgProtoBufHeader.newBuilder();
                MsgHeaderProtoBuf header = new MsgHeaderProtoBuf(proto);
                header.msg = EMsg.ClientLogon;
                SteammessagesClientserver.CMsgClientLogon.Builder body
                        = SteammessagesClientserver.CMsgClientLogon.newBuilder();
                MsgProtoBuf msg = new MsgProtoBuf(header, body);

                proto.setSteamid(new SteamId(0, credentials.accountInstance, connectedUniverse, EAccountType.Individual).toLong());

                body.setAccountName(credentials.username);
                body.setPassword(credentials.password);
                body.setProtocolVersion(65575);

                if (credentials.authCode.length() > 0) {
                    body.setAuthCode(credentials.authCode);
                }

                if (credentials.acceptSentry) {
                    Path sentryFile = Paths.get("sentry.bin");
                    if (sentryFile.toFile().exists()) {
                        byte[] sentryBytes = Files.readAllBytes(sentryFile);
                        byte[] shaHash = CryptoUtil.shaHash(sentryBytes);
                        if (shaHash != null) {
                            body.setShaSentryfile(ByteString.copyFrom(shaHash));
                            body.setEresultSentryfile(EResult.OK.getCode());
                        }
                    } else {
                        body.clearShaSentryfile();
                        body.setEresultSentryfile(EResult.FileNotFound.getCode());
                    }

                    registerSentryListener();
                }

                byte[] encodedMsg = msg.encode();
                connection.sendPacket(encodedMsg);
            } catch (IOException | GeneralSecurityException e) {
                Throwables.propagate(e);
            }
        } else {
            System.out.println("A username and password must be specified in order to login.");
        }
    }

    private void registerSentryListener() {
        this.registerEventHandler(EMsg.ClientUpdateMachineAuth, (d) -> {
            MsgHeaderProtoBuf header = new MsgHeaderProtoBuf();
            SteammessagesClientserver2.CMsgClientUpdateMachineAuth.Builder body
                    = SteammessagesClientserver2.CMsgClientUpdateMachineAuth.newBuilder();
            try {
                new MsgProtoBuf(header, body).decode(d);
            } catch (IOException e) {
                Throwables.propagate(e);
            }

            Path sentryFile = Paths.get("sentry.bin");
            byte[] sentryBytes = body.getBytes().toByteArray();
            try {
                Files.write(sentryFile, sentryBytes);
            } catch (IOException e) {
                Throwables.propagate(e);
            }

            SteammessagesBase.CMsgProtoBufHeader.Builder proto
                    = SteammessagesBase.CMsgProtoBufHeader.newBuilder();
            MsgHeaderProtoBuf responseHeader = new MsgHeaderProtoBuf(proto);
            responseHeader.msg = EMsg.ClientUpdateMachineAuthResponse;
            SteammessagesClientserver2.CMsgClientUpdateMachineAuthResponse.Builder responseBody
                    = SteammessagesClientserver2.CMsgClientUpdateMachineAuthResponse.newBuilder();
            MsgProtoBuf msg = new MsgProtoBuf(responseHeader, responseBody);

            proto.setSteamid(steamId);
            proto.setJobidTarget(header.proto.getJobidSource());

            responseBody.setFilenameBytes(body.getFilenameBytes());
            responseBody.setFilesize(sentryBytes.length);
            byte[] shaHash = CryptoUtil.shaHash(sentryBytes);
            responseBody.setShaFile(ByteString.copyFrom(shaHash));

            responseBody.setCubwrote(body.getCubtowrite());
            responseBody.setOffset(body.getOffset());

            responseBody.setEresult(EResult.OK.getCode());
            responseBody.setGetlasterror(0);

            responseBody.setOtpType(body.getOtpType());
            responseBody.setOtpValue(0);
            responseBody.setOtpIdentifierBytes(body.getOtpIdentifierBytes());

            try {
                byte[] encodedMsg = msg.encode();
                connection.sendPacket(encodedMsg);

                System.out.println("Successfully updated sentry file for machine authentication.");
            } catch (IOException | GeneralSecurityException e) {
                Throwables.propagate(e);
            }
        });
    }

    public void registerEventHandler(EMsg eventMsg, EventHandler eventHandler) {
        EventListener eventListener = eventListeners.get(eventMsg);
        if (eventListener == null) {
            eventListener = new EventListener();
            eventListeners.put(eventMsg, eventListener);
        }
        eventListener.registerHandler(eventHandler);
    }
}
