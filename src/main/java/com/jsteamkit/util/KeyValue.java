package com.jsteamkit.util;

import com.jsteamkit.internals.stream.BinaryReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyValue {

    public String name;
    public String value;

    public List<KeyValue> children = new ArrayList<>();

    public boolean readAsBinary(BinaryReader reader) throws IOException {
        while (true) {
            byte typeAsByte = reader.readByte();
            Type type = Type.get(typeAsByte);
            if (type == null || type == Type.End) {
                break;
            }

            KeyValue current = new KeyValue();
            current.name = reader.readString();

            switch (type) {
                case None:
                    current.readAsBinary(reader);
                    break;
                case String:
                    current.value = reader.readString();
                    break;
                case WideString:
                    throw new IOException("WideString is not supported");
                case Int32:
                case Color:
                case Pointer:
                    current.value = reader.readInt() + "";
                    break;
                case UInt64:
                    current.value = reader.readLong() + "";
                    break;
                case Float32:
                    current.value = reader.readFloat() + "";
                    break;
                case Int64:
                    current.value = reader.readLong() + "";
                    break;
                default:
                    throw new IOException("Unknown KeyValue type");
            }

            children.add(current);
        }

        return reader.reader.isAtEnd();
    }

    public KeyValue getChild(String name) {
        for (KeyValue child : children) {
            if (child.name.equals(name)) {
                return child;
            }
        }
        return null;
    }

    private enum Type {

        None(0),
        String(1),
        Int32(2),
        Float32(3),
        Pointer(4),
        WideString(5),
        Color(6),
        UInt64(7),
        End(8),
        Int64(10);

        private byte code;

        Type(int code) {
            this.code = (byte) code;
        }

        public int getCode() {
            return code;
        }

        private static Map<Byte, Type> values = new HashMap<>();
        static {
            for (Type keyValueType : Type.values()) {
                values.put(keyValueType.code, keyValueType);
            }
        }

        public static Type get(byte code) {
            return values.get(code);
        }
    }
}
