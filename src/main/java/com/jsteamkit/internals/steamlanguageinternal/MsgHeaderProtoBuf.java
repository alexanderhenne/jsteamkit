package com.jsteamkit.internals.steamlanguageinternal;

import com.jsteamkit.internals.proto.SteammessagesBase;
import com.jsteamkit.internals.steamlanguage.EMsg;
import com.jsteamkit.internals.stream.BinaryReader;
import com.jsteamkit.internals.stream.BinaryWriter;
import com.jsteamkit.util.MsgUtil;

import java.io.IOException;

public class MsgHeaderProtoBuf extends SerializableMsg {

    public EMsg msg = EMsg.Invalid;

    public int headerLen = 0;
    public SteammessagesBase.CMsgProtoBufHeader.Builder proto;

    public MsgHeaderProtoBuf(SteammessagesBase.CMsgProtoBufHeader.Builder proto) {
        this.proto = proto;
    }

    public MsgHeaderProtoBuf() {
    }

    @Override
    public void encode(BinaryWriter writer) throws IOException {
        byte[] protoBufArray = proto.build().toByteArray();
        headerLen = protoBufArray.length;
        writer.write(MsgUtil.makeMsg(msg.getCode(), true));
        writer.write(headerLen);
        writer.write(protoBufArray);
    }

    @Override
    public void decode(BinaryReader reader) throws IOException {
        msg = MsgUtil.getMsg(reader.readInt());
        headerLen = reader.readInt();
        proto = SteammessagesBase.CMsgProtoBufHeader.newBuilder();
        proto.mergeFrom(reader.readBytes(headerLen));
    }
}
