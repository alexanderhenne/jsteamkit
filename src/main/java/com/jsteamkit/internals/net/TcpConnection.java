package com.jsteamkit.internals.net;

import com.google.common.base.Throwables;
import com.jsteamkit.cm.CMServer;
import com.jsteamkit.internals.steamlanguage.EMsg;
import com.jsteamkit.internals.steamlanguageinternal.ExtendedMsgHeaderProtoBuf;
import com.jsteamkit.internals.steamlanguageinternal.MsgHeader;
import com.jsteamkit.internals.steamlanguageinternal.MsgHeaderProtoBuf;
import com.jsteamkit.internals.steamlanguageinternal.SerializableMsg;
import com.jsteamkit.internals.stream.BinaryReader;
import com.jsteamkit.internals.stream.BinaryWriter;
import com.jsteamkit.util.CryptoUtil;
import com.jsteamkit.util.MsgUtil;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.text.MessageFormat;

public abstract class TcpConnection {

    private Socket socket;
    private Thread netThread;
    private BinaryReader reader;
    private BinaryWriter writer;

    private final Object writeLock = new Object();

    public byte[] sessionKey;
    public boolean encrypted;

    public void connect(CMServer server) throws IOException {
        connect(server.ip, server.port);
    }

    public void connect(String ip, int port) throws IOException {
        if (socket != null) {
            disconnect();
        }

        socket = new Socket(ip, port);

        reader = new BinaryReader(socket.getInputStream());
        synchronized (writeLock) {
            writer = new BinaryWriter(socket.getOutputStream());
        }

        launchNetThread();
    }

    private void launchNetThread() {
        netThread = new Thread(() -> {
            while (socket != null
                    && !socket.isClosed()
                    && socket.isConnected()) {
                try {
                    if (!reader.reader.isAtEnd()) {
                        readPacket();
                    }
                } catch (IOException | IllegalStateException e) {
                    throw Throwables.propagate(e);
                }
            }
        });
        netThread.start();
    }

    private void readPacket() throws IOException, IllegalStateException {
        int len = reader.readInt();
        int magic = reader.readInt();

        if (magic != 0x31305456) {
            throw new IllegalStateException(MessageFormat.format("Error: Expected magic {0} but received {1}!",
                    0x31305456,
                    magic));
        }

        byte[] data = reader.readBytes(len);
        if (len != data.length) {
            throw new IllegalStateException(MessageFormat.format("Error: Length of received bytes {0} did not match the expected length of {1}!",
                    data.length,
                    len));
        }

        if (encrypted) {
            try {
                data = CryptoUtil.decryptSymmetrically(data, sessionKey);
            } catch (GeneralSecurityException e) {
                Throwables.propagate(e);
            }
        }

        handleMsg(data);
    }

    public void handleMsg(byte[] data) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        int rawEMsg = byteBuffer.getInt();
        EMsg eMsg = MsgUtil.getMsg(rawEMsg);
        if (eMsg != null) {
            SerializableMsg header;

            if (eMsg == EMsg.ChannelEncryptRequest
                    || eMsg == EMsg.ChannelEncryptResponse
                    || eMsg == EMsg.ChannelEncryptResult) {
                header = new MsgHeader();
            } else if (MsgUtil.isProtoBuf(rawEMsg)) {
                header = new MsgHeaderProtoBuf();
            } else {
                // Struct message
                header = new ExtendedMsgHeaderProtoBuf();
            }

            try {
                header.decode(new BinaryReader(data));
            } catch (IOException e) {
                Throwables.propagate(e);
            }

            handleEvent(eMsg, data);
        }
    }

    public abstract void handleEvent(EMsg eventMsg, byte[] data);

    public void sendPacket(byte[] data)
            throws IOException, NoSuchAlgorithmException, NoSuchPaddingException,
            NoSuchProviderException, InvalidKeySpecException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {

        if (encrypted) {
            data = CryptoUtil.encryptSymmetrically(data, sessionKey);
        }

        synchronized (writeLock) {
            writer.write(data.length);
            writer.write(0x31305456);
            writer.write(data);
            writer.flush();
        }
    }

    public void disconnect() throws IOException {
        if (socket != null) {
            socket.close();
            socket = null;
        }
        if (netThread != null) {
            try {
                netThread.join();
                netThread = null;
            } catch (InterruptedException e) {
                Throwables.propagate(e);
            }
        }
    }
}
