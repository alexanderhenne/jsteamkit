package com.jsteamkit.internals.steamlanguageinternal;

import com.jsteamkit.internals.steamlanguage.EMsg;
import com.jsteamkit.internals.stream.BinaryReader;
import com.jsteamkit.internals.stream.BinaryWriter;
import com.jsteamkit.util.MsgUtil;

import java.io.IOException;

public class ExtendedMsgHeaderProtoBuf extends SerializableMsg {

    public EMsg msg = EMsg.Invalid;

    public byte headerSize = 36;
    public int headerVersion = 36;
    public byte headerCanary = (byte) 239;

    public long steamId = 0;
    public long sessionId = 0;

    public long targetJobId = 0xFFFFFFFFFFFFFFFFL;
    public long sourceJobId = 0xFFFFFFFFFFFFFFFFL;

    @Override
    public void encode(BinaryWriter writer) throws IOException {
        writer.write(msg.getCode());
        writer.write(headerSize);
        writer.write(headerVersion);
        writer.write(targetJobId);
        writer.write(sourceJobId);
        writer.write(headerCanary);
        writer.write(steamId);
        writer.write(sessionId);
    }

    @Override
    public void decode(BinaryReader reader) throws IOException {
        msg = MsgUtil.getMsg(reader.readInt());
        headerSize = reader.readByte();
        headerVersion = reader.readShort();
        targetJobId = reader.readLong();
        sourceJobId = reader.readLong();
        headerCanary = reader.readByte();
        steamId = reader.readLong();
        sessionId = reader.readInt();
    }
}
