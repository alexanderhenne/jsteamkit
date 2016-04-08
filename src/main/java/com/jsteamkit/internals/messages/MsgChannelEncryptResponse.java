package com.jsteamkit.internals.messages;

import com.jsteamkit.internals.steamlanguageinternal.SerializableMsg;
import com.jsteamkit.internals.stream.BinaryReader;
import com.jsteamkit.internals.stream.BinaryWriter;

import java.io.IOException;

public class MsgChannelEncryptResponse extends SerializableMsg {

    public int protocolVersion = 1;
    public int keySize = 128;

    @Override
    public void encode(BinaryWriter writer) throws IOException {
        writer.write(protocolVersion);
        writer.write(keySize);
    }

    @Override
    public void decode(BinaryReader reader) throws IOException {
        protocolVersion = reader.readInt();
        keySize = reader.readInt();
    }
}
