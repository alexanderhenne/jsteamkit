package com.jsteamkit.internals.stream;

import com.google.common.base.Throwables;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class BinaryReader {

    public CodedInputStream reader;
    int len = 0;

    public BinaryReader(InputStream stream) {
        reader = CodedInputStream.newInstance(stream);
    }

    public BinaryReader(byte[] data) {
        reader = CodedInputStream.newInstance(data);
        len = data.length;
        try {
            reader.pushLimit(len);
        } catch (InvalidProtocolBufferException e) {
            Throwables.propagate(e);
        }
    }


    public String readString() throws IOException {
        byte[] buffer = new byte[len];
        int i = 0;
        byte rb;
        while (i < len) {
            if ((rb = reader.readRawByte()) != 0) {
                buffer[i++] = rb;
            } else {
                break;
            }
        }
        return new String(buffer, 0, i);
    }

    public long readLong() throws IOException {
        return getBuffer(8).getLong();
    }

    public double readDouble() throws IOException {
        return getBuffer(8).getDouble();
    }

    public int readInt() throws IOException {
        return getBuffer(4).getInt();
    }

    public float readFloat() throws IOException {
        return getBuffer(4).getFloat();
    }

    public short readShort() throws IOException {
        return getBuffer(2).getShort();
    }

    public byte readByte() throws IOException {
        return reader.readRawByte();
    }

    public byte[] readBytes() throws IOException {
        return reader.readRawBytes(getRemaining());
    }

    public byte[] readBytes(int length) throws IOException {
        return reader.readRawBytes(length);
    }

    public int getPosition() {
        return reader.getTotalBytesRead();
    }

    public int getRemaining() {
        return len - getPosition();
    }

    private ByteBuffer getBuffer(int len) throws IOException {
        byte[] buffer = new byte[len];
        for (int i = 1; i <= len; i++) {
            buffer[len - i] = reader.readRawByte();
        }
        return ByteBuffer.wrap(buffer);
    }
}
