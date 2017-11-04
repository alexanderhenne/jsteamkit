package com.jsteamkit.internals.stream;

import com.google.protobuf.CodedOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class BinaryWriter {

    public OutputStream outputStream;
    public CodedOutputStream writer;

    public BinaryWriter(OutputStream stream) {
        this.outputStream = stream;
        writer = CodedOutputStream.newInstance(stream);
    }

    public BinaryWriter(int size) {
        this(new ByteArrayOutputStream(size));
    }

    public void write(long data) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        byteBuffer.putLong(data);
        writeBuffer(byteBuffer);
    }

    public void write(double data) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        byteBuffer.putDouble(data);
        writeBuffer(byteBuffer);
    }

    public void write(int data) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putInt(data);
        writeBuffer(byteBuffer);
    }

    public void write(float data) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putFloat(data);
        writeBuffer(byteBuffer);
    }

    public void write(short data) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(2);
        byteBuffer.putShort(data);
        writeBuffer(byteBuffer);
    }

    public void write(byte[] data) throws IOException {
        writer.writeRawBytes(data);
        writer.flush();
    }

    public void write(byte data) throws IOException {
        writer.writeRawByte(data);
        writer.flush();
    }

    public void writeBuffer(ByteBuffer byteBuffer) throws IOException {
        for (int i = byteBuffer.capacity() - 1; i >= 0; --i) {
            write(byteBuffer.get(i));
        }
    }

    public byte[] toByteArray() {
        if (outputStream instanceof ByteArrayOutputStream) {
            return ((ByteArrayOutputStream) outputStream).toByteArray();
        }
        return null;
    }

    public void flush() {
        try {
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
