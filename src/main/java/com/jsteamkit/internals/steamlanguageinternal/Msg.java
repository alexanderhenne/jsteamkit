package com.jsteamkit.internals.steamlanguageinternal;

import com.jsteamkit.internals.stream.BinaryReader;
import com.jsteamkit.internals.stream.BinaryWriter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

public class Msg {

    public MsgHeader header;
    public SerializableMsg body;
    public BinaryReader reader;
    public BinaryWriter writer;

    public Msg(MsgHeader header, SerializableMsg body) {
        this(header, body, 64);
    }

    public Msg(MsgHeader header, SerializableMsg body, int payloadSize) {
        this.header = header;
        this.body = body;
        this.writer = new BinaryWriter(payloadSize);
    }

    public byte[] encode() throws IOException {
        BinaryWriter binaryWriter = new BinaryWriter(32);

        header.encode(binaryWriter);
        body.encode(binaryWriter);
        binaryWriter.write(writer.toByteArray());

        return binaryWriter.toByteArray();
    }

    public void decode(byte[] data) throws IOException {
        BinaryReader binaryReader = new BinaryReader(data);
        header.decode(binaryReader);
        body.decode(binaryReader);

        int payloadOffset = binaryReader.getPosition();
        int payloadLen = binaryReader.getRemaining();

        reader = new BinaryReader(new ByteArrayInputStream(Arrays.copyOfRange(data, payloadOffset, payloadOffset + payloadLen)));
    }
}
