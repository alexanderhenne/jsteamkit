package com.jsteamkit.internals.steamlanguage;

public enum EAccountType {

    Invalid(0),
    Individual(1),
    MultiSeat(2),
    GameServer(3),
    AnonGameServer(4),
    Pending(5),
    ContentServer(6),
    Clan(7),
    Chat(8),
    ConsoleUser(9),
    AnonUser(10),
    Max(11);

    private int code;

    EAccountType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    private static EAccountType[] values = new EAccountType[12];
    static {
        for (EAccountType eAccountType : EAccountType.values()) {
            values[eAccountType.code] = eAccountType;
        }
    }

    public static EAccountType get(int code) {
        if (code < values.length) {
            return values[code];
        }
        return null;
    }
}
