package com.jsteamkit.util;

public class BitVector64 {

    public Long value;

    public BitVector64(long value) {
        this.value = value;
    }

    public long getMask(short bitOffset, int valueMask) {
        return value >> bitOffset & valueMask;
    }

    public void setMask(short bitOffset, long valueMask, long value) {
        this.value = (this.value & ~(valueMask << bitOffset)) | ((value & valueMask) << bitOffset);
    }
}
