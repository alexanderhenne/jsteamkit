package com.jsteamkit.internals.messages;

import com.jsteamkit.internals.steamlanguage.EResult;
import com.jsteamkit.internals.steamlanguageinternal.SerializableMsg;
import com.jsteamkit.internals.stream.BinaryReader;
import com.jsteamkit.internals.stream.BinaryWriter;

import java.io.IOException;

public class MsgChannelEncryptResult extends SerializableMsg {

    public EResult result = EResult.Invalid;

    @Override
    public void encode(BinaryWriter writer) throws IOException {
        writer.write(result.getCode());
    }

    @Override
    public void decode(BinaryReader reader) throws IOException {
        result = EResult.get(reader.readInt());
    }
}
