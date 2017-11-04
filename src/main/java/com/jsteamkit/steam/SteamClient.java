package com.jsteamkit.steam;

import com.google.protobuf.ByteString;
import com.jsteamkit.cm.CMServer;
import com.jsteamkit.cm.CMServerList;
import com.jsteamkit.event.EventHandler;
import com.jsteamkit.event.EventListener;
import com.jsteamkit.internals.messages.MsgChannelEncryptRequest;
import com.jsteamkit.internals.messages.MsgChannelEncryptResponse;
import com.jsteamkit.internals.messages.MsgChannelEncryptResult;
import com.jsteamkit.internals.net.TcpConnection;
import com.jsteamkit.internals.proto.SteammessagesBase;
import com.jsteamkit.internals.proto.SteammessagesClientserver2;
import com.jsteamkit.internals.proto.SteammessagesClientserverLogin;
import com.jsteamkit.internals.steamlanguage.EAccountType;
import com.jsteamkit.internals.steamlanguage.EMsg;
import com.jsteamkit.internals.steamlanguage.EResult;
import com.jsteamkit.internals.steamlanguage.EUniverse;
import com.jsteamkit.internals.steamlanguageinternal.Msg;
import com.jsteamkit.internals.steamlanguageinternal.MsgHeader;
import com.jsteamkit.internals.steamlanguageinternal.MsgHeaderProtoBuf;
import com.jsteamkit.internals.steamlanguageinternal.MsgProtoBuf;
import com.jsteamkit.internals.stream.BinaryReader;
import com.jsteamkit.steam.guard.SteamAuthenticator;
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
import java.util.Timer;
import java.util.TimerTask;

public class SteamClient {

    private TcpConnection connection;
    private EUniverse connectedUniverse = EUniverse.Invalid;
    private long steamId;

    private Map<EMsg, EventListener> eventListeners = new HashMap<>();
    private Timer heartBeatTimer;

    public SteamClient() {
        this.registerEventHandler(EMsg.ChannelEncryptRequest, (d) -> {
            MsgChannelEncryptRequest body = new MsgChannelEncryptRequest();
            try {
                new Msg(new MsgHeader(), body).decode(d);
            } catch (IOException e) {
                throw new RuntimeException(e);
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
                    throw new RuntimeException(e);
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
                throw new RuntimeException(e);
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
                throw new RuntimeException(e);
            }

            byte[] payload = body.getMessageBody().toByteArray();

            if (body.getSizeUnzipped() > 0) {
                try {
                    payload = ZipUtil.unzip(payload);
                } catch (IOException e) {
                    throw new RuntimeException(e);
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
                throw new RuntimeException(e);
            }
        });

        this.registerEventHandler(EMsg.ClientLogOnResponse, (d) -> {
            MsgHeaderProtoBuf header = new MsgHeaderProtoBuf();
            SteammessagesClientserverLogin.CMsgClientLogonResponse.Builder body
                    = SteammessagesClientserverLogin.CMsgClientLogonResponse.newBuilder();
            try {
                new MsgProtoBuf(header, body).decode(d);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            EResult result = EResult.get(body.getEresult());
            if (result == EResult.OK) {
                steamId = header.proto.getSteamid();

                heartBeatTimer = new Timer();
                heartBeatTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            SteammessagesBase.CMsgProtoBufHeader.Builder proto
                                    = SteammessagesBase.CMsgProtoBufHeader.newBuilder();
                            MsgHeaderProtoBuf header = new MsgHeaderProtoBuf(proto);
                            header.msg = EMsg.ClientHeartBeat;
                            SteammessagesClientserverLogin.CMsgClientHeartBeat.Builder body
                                    = SteammessagesClientserverLogin.CMsgClientHeartBeat.newBuilder();
                            MsgProtoBuf msg = new MsgProtoBuf(header, body);

                            byte[] encodedMsg = msg.encode();
                            connection.sendPacket(encodedMsg);
                        } catch (IOException | GeneralSecurityException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, 0, body.getOutOfGameHeartbeatSeconds() * 1000);

                System.out.println("Successfully logged in. Steam ID: " + steamId);
            } else {
                System.out.println("Failed login. Result: " + result);
            }
        });

        this.registerEventHandler(EMsg.ClientLoggedOff, (d) -> {
            MsgHeaderProtoBuf header = new MsgHeaderProtoBuf();
            SteammessagesClientserverLogin.CMsgClientLoggedOff.Builder body
                    = SteammessagesClientserverLogin.CMsgClientLoggedOff.newBuilder();
            try {
                new MsgProtoBuf(header, body).decode(d);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (heartBeatTimer != null) {
                heartBeatTimer.cancel();
                heartBeatTimer = null;
            }

            System.out.println("Logged out of Steam. Result: " + EResult.get(body.getEresult()));
        });
    }

    public void connect(boolean verbose)
            throws IOException {

        connect(verbose, CMServerList.getBestServer());
    }

    public void connect(boolean verbose, CMServer cmServer)
            throws IOException {

        connection = new TcpConnection() {
            @Override
            public void handleEvent(EMsg eventMsg, byte[] data) {
                if (verbose) {
                    System.out.println("Received msg: " + eventMsg);
                }

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
                SteammessagesClientserverLogin.CMsgClientLogon.Builder body
                        = SteammessagesClientserverLogin.CMsgClientLogon.newBuilder();
                MsgProtoBuf msg = new MsgProtoBuf(header, body);

                proto.setSteamid(new SteamId(0, credentials.accountInstance, connectedUniverse, EAccountType.Individual).toLong());

                body.setAccountName(credentials.username);
                body.setPassword(credentials.password);
                body.setProtocolVersion(65575);

                if (credentials.authCode.length() > 0) {
                    body.setAuthCode(credentials.authCode);
                }

                if (credentials.authenticatorSecret.length() > 0) {
                    SteamAuthenticator authenticator = new SteamAuthenticator(credentials.authenticatorSecret);
                    body.setTwoFactorCode(authenticator.generateCode());
                }

                if (credentials.acceptSentry) {
                    Path sentryFile = Paths.get("data/sentry.bin");
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
                throw new RuntimeException(e);
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
                throw new RuntimeException(e);
            }

            Path sentryFile = Paths.get("data/sentry.bin");
            byte[] sentryBytes = body.getBytes().toByteArray();
            try {
                Files.createDirectories(sentryFile.getParent());
                Files.write(sentryFile, sentryBytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
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
                throw new RuntimeException(e);
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

    public TcpConnection getConnection() {
        return connection;
    }

    public EUniverse getConnectedUniverse() {
        return connectedUniverse;
    }

    public long getSteamId() {
        return steamId;
    }
}
