package com.jsteamkit.internals.steamlanguageinternal;

import com.jsteamkit.internals.stream.BinaryReader;
import com.jsteamkit.internals.stream.BinaryWriter;

import java.io.IOException;

public abstract class SerializableMsg {

    public abstract void encode(BinaryWriter writer) throws IOException;

    public abstract void decode(BinaryReader reader) throws IOException;
}
