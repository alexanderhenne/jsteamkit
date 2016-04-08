package com.jsteamkit.internals.messages;

import com.jsteamkit.internals.steamlanguage.EUniverse;
import com.jsteamkit.internals.steamlanguageinternal.SerializableMsg;
import com.jsteamkit.internals.stream.BinaryReader;
import com.jsteamkit.internals.stream.BinaryWriter;

import java.io.IOException;

public class MsgChannelEncryptRequest extends SerializableMsg {

    public int protocolVersion = 1;
    public EUniverse universe = EUniverse.Invalid;

    @Override
    public void encode(BinaryWriter writer) throws IOException {
        writer.write(protocolVersion);
        writer.write(universe.getCode());
    }

    @Override
    public void decode(BinaryReader reader) throws IOException {
        protocolVersion = reader.readInt();
        universe = EUniverse.get(reader.readInt());
    }
}
