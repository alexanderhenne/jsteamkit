package com.jsteamkit.util;

import com.jsteamkit.internals.stream.BinaryReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class ZipUtil {

    private static final int localFileHeader = 0x04034b50;
    private static final int centralDirectoryHeader = 0x02014b50;
    private static final int endOfDirectoryHeader = 0x06054b50;

    private static final short deflateCompression = 8;

    public static byte[] unzip(byte[] buffer) throws IOException {
        BinaryReader reader = new BinaryReader(buffer);

        if (reader.readInt() != localFileHeader) {
            throw new IOException("Expecting localFileHeader at start of stream");
        }

        byte[] compressedBuffer = ZipUtil.readLocalFile(reader);

        if (reader.readInt() != centralDirectoryHeader) {
            throw new IOException("Expecting CentralDirectoryHeader following filename");
        }

        ZipUtil.readCentralDirectory(reader);

        if (reader.readInt() != endOfDirectoryHeader) {
            throw new IOException("Expecting EndOfDirectoryHeader following CentralDirectoryHeader");
        }

        readEndOfDirectory(reader);

        return ZipUtil.inflateData(compressedBuffer);
    }

    private static void readEndOfDirectory(BinaryReader reader) throws IOException {
        short diskNumber = reader.readShort();
        short cdrDisk = reader.readShort();
        short cdrCount = reader.readShort();
        short cdrTotal = reader.readShort();

        int cdrSize = reader.readInt();
        int cdrOffset = reader.readInt();

        short commentLength = reader.readShort();
        byte[] comment = reader.readBytes(commentLength);
    }

    private static void readCentralDirectory(BinaryReader reader) throws IOException {
        short versionGenerator = reader.readShort();
        short versionExtract = reader.readShort();
        short bitflags = reader.readShort();
        short compression = reader.readShort();

        if (compression != deflateCompression) {
            throw new IOException("Invalid compression method " + compression);
        }

        short modTime = reader.readShort();
        short createTime = reader.readShort();
        int crc32 = reader.readInt();

        int zippedSize = reader.readInt();
        int unzippedSize = reader.readInt();

        short nameLength = reader.readShort();
        short fieldLength = reader.readShort();
        short commentLength = reader.readShort();

        short diskNumber = reader.readShort();
        short internalAttributes = reader.readShort();
        int externalAttributes = reader.readInt();

        int relativeOffset = reader.readInt();

        byte[] name = reader.readBytes(nameLength);
        byte[] fields = reader.readBytes(fieldLength);
        byte[] comment = reader.readBytes(commentLength);
    }

    private static byte[] readLocalFile(BinaryReader reader) throws IOException {
        short version = reader.readShort();
        short bitFlags = reader.readShort();
        short compression = reader.readShort();

        if (compression != deflateCompression) {
            throw new IOException("Invalid compression method " + compression);
        }

        short modTime = reader.readShort();
        short createTime = reader.readShort();
        int crc32 = reader.readInt();

        int zippedSize = reader.readInt();
        int unzippedSize = reader.readInt();

        short nameLength = reader.readShort();
        short fieldLength = reader.readShort();

        byte[] name = reader.readBytes(nameLength);
        byte[] fields = reader.readBytes(fieldLength);

        return reader.readBytes(zippedSize);
    }

    private static byte[] inflateData(byte[] data) throws IOException {
        Inflater inflater = new Inflater(true);
        inflater.setInput(data);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);

        byte[] buffer = new byte[1024];
        while (!inflater.finished()) {
            try {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            } catch (DataFormatException e) {
                //swallow
            }
        }

        return outputStream.toByteArray();
    }
}
