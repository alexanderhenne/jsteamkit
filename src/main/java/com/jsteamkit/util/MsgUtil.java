package com.jsteamkit.util;

import com.jsteamkit.internals.steamlanguage.EMsg;

public class MsgUtil {

    private static final int protoMask = 0x80000000;
    private static final int eMsgMask = ~protoMask;

    public static EMsg getMsg(int msg) {
        return EMsg.get(msg & eMsgMask);
    }

    public static int makeMsg(int msg, boolean isProtoBuf) {
        return isProtoBuf ? msg | protoMask : msg;
    }

    public static boolean isProtoBuf(int msg) {
        return (msg & 0xFFFFFFFFL & protoMask) > 0;
    }
}
