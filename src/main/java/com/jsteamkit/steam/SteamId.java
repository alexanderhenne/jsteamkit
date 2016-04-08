package com.jsteamkit.steam;

import com.jsteamkit.internals.steamlanguage.EAccountType;
import com.jsteamkit.internals.steamlanguage.EUniverse;
import com.jsteamkit.util.BitVector64;

public class SteamId {

    private BitVector64 id;

    public SteamId(long id) {
        this.id = new BitVector64(id);
    }

    public SteamId(int accountId, int instance, EUniverse universe, EAccountType accountType) {
        this(0);
        setAccountId(accountId);
        setInstance(instance);
        setUniverse(universe);
        setAccountType(accountType);
    }

    public void setAccountId(long value) {
        id.setMask((short) 0, 0xFFFFFFFF, value);
    }

    public long getAccountId() {
        return id.getMask((short) 0, 0xFFFFFFFF);
    }

    public void setInstance(long value) {
        id.setMask((short) 32, 0xFFFFF, value);
    }

    public long getInstance() {
        return id.getMask((short) 32, 0xFFFFF);
    }

    public void setUniverse(EUniverse universe) {
        id.setMask((short) 56, 0xFF, universe.getCode());
    }

    public EUniverse getUniverse() {
        return EUniverse.get((int) id.getMask((short) 56, 0xFF));
    }

    public void setAccountType(EAccountType accountType) {
        id.setMask((short) 52, 0xF, accountType.getCode());
    }

    public EAccountType getAccountType() {
        return EAccountType.get((int) id.getMask((short) 52, 0xF));
    }

    public long toLong() {
        return id.value;
    }
}
