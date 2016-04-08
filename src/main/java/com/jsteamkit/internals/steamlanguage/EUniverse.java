package com.jsteamkit.internals.steamlanguage;

public enum  EUniverse {

    Invalid(0),
    Public(1),
    Beta(2),
    Internal(3),
    Dev(4),
    RC(5),
    Max(6);

    private int code;

    EUniverse(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    private static EUniverse[] values = new EUniverse[7];
    static {
        for (EUniverse eUniverse : EUniverse.values()) {
            values[eUniverse.code] = eUniverse;
        }
    }

    public static EUniverse get(int code) {
        return values[code];
    }
}
