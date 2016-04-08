package com.jsteamkit.internals.steamlanguageinternal;

import com.jsteamkit.internals.steamlanguage.EMsg;
import com.jsteamkit.internals.stream.BinaryReader;
import com.jsteamkit.internals.stream.BinaryWriter;

import java.io.IOException;

public class MsgHeader extends SerializableMsg {

    public EMsg msg = EMsg.Invalid;

    public long targetJobId = 0xFFFFFFFFFFFFFFFFL;
    public long sourceJobId = 0xFFFFFFFFFFFFFFFFL;

    @Override
    public void encode(BinaryWriter writer) throws IOException {
        writer.write(msg.getCode());
        writer.write(targetJobId);
        writer.write(sourceJobId);
    }

    @Override
    public void decode(BinaryReader reader) throws IOException {
        msg = EMsg.get(reader.readInt());
        targetJobId = reader.readLong();
        sourceJobId = reader.readLong();
    }
}
