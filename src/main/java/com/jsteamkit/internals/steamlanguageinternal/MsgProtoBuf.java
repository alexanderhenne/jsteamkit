package com.jsteamkit.internals.steamlanguageinternal;

import com.google.protobuf.GeneratedMessageV3;
import com.jsteamkit.internals.stream.BinaryReader;
import com.jsteamkit.internals.stream.BinaryWriter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

public class MsgProtoBuf {

    public MsgHeaderProtoBuf header;
    public GeneratedMessageV3.Builder body;
    public BinaryReader reader;

    public int headerLen = 0;

    public MsgProtoBuf(MsgHeaderProtoBuf header, GeneratedMessageV3.Builder body) {
        this.header = header;
        this.body = body;
    }

    public byte[] encode() throws IOException {
        BinaryWriter binaryWriter = new BinaryWriter(32);

        header.encode(binaryWriter);
        byte[] protoBufArray = body.build().toByteArray();
        headerLen = protoBufArray.length;
        binaryWriter.write(protoBufArray);

        return binaryWriter.toByteArray();
    }

    public void decode(byte[] data) throws IOException {
        BinaryReader binaryReader = new BinaryReader(data);
        header.decode(binaryReader);
        body.mergeFrom(binaryReader.readBytes());

        int payloadOffset = binaryReader.getPosition();
        int payloadLen = binaryReader.getRemaining();

        reader = new BinaryReader(new ByteArrayInputStream(Arrays.copyOfRange(data, payloadOffset, payloadOffset + payloadLen)));
    }
}
