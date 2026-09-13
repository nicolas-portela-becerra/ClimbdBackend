package com.climbingapp.application.usecase;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/*AI Generated code, image pipeline being rebuilt manually*/
final class ExifOrientation {

    private static final int JPEG_SOI = 0xFFD8;
    private static final int APP1_MARKER = 0xE1;
    private static final int SOS_MARKER = 0xDA;
    private static final int ORIENTATION_TAG = 0x0112;
    private static final byte[] EXIF_HEADER = {'E', 'x', 'i', 'f', 0, 0};

    private ExifOrientation() {}

    static int read(byte[] imageData) {
        try {
            byte[] tiffBlock = findExifTiffBlock(imageData);
            return tiffBlock == null ? 1 : parseOrientation(tiffBlock);
        } catch (Exception e) {
            return 1;
        }
    }

    private static byte[] findExifTiffBlock(byte[] data) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
        if (in.readUnsignedShort() != JPEG_SOI) {
            return null; // not a JPEG — PNG/WebP carry no EXIF orientation we handle
        }
        while (true) {
            int prefix = in.readUnsignedByte();
            if (prefix != 0xFF) {
                return null; // corrupt marker structure
            }
            int marker = in.readUnsignedByte();
            if (marker == SOS_MARKER) {
                return null; // reached image data — no EXIF found
            }
            if (marker == 0x01 || (marker >= 0xD0 && marker <= 0xD9)) {
                continue; // standalone markers without payload
            }
            int length = in.readUnsignedShort();
            if (length < 2) {
                return null; // corrupt segment
            }
            if (marker == APP1_MARKER) {
                byte[] segment = new byte[length - 2];
                in.readFully(segment);
                if (segment.length > EXIF_HEADER.length
                        && Arrays.equals(Arrays.copyOf(segment, EXIF_HEADER.length), EXIF_HEADER)) {
                    return Arrays.copyOfRange(segment, EXIF_HEADER.length, segment.length);
                }
                // APP1 but not Exif — keep walking
            } else {
                skipFully(in, length - 2);
            }
        }
    }

    private static int parseOrientation(byte[] tiff) {
        if (tiff.length < 8) {
            return 1;
        }
        ByteOrder order;
        if (tiff[0] == 'I' && tiff[1] == 'I') {
            order = ByteOrder.LITTLE_ENDIAN;
        } else if (tiff[0] == 'M' && tiff[1] == 'M') {
            order = ByteOrder.BIG_ENDIAN;
        } else {
            return 1;
        }
        ByteBuffer buffer = ByteBuffer.wrap(tiff).order(order);
        if (buffer.getShort(2) != 42) {
            return 1;
        }
        int ifdOffset = buffer.getInt(4);
        if (ifdOffset < 0 || ifdOffset + 2 > tiff.length) {
            return 1;
        }
        int entries = buffer.getShort(ifdOffset) & 0xFFFF;
        for (int i = 0; i < entries; i++) {
            int entryOffset = ifdOffset + 2 + i * 12;
            if (entryOffset + 12 > tiff.length) {
                return 1;
            }
            int tag = buffer.getShort(entryOffset) & 0xFFFF;
            if (tag == ORIENTATION_TAG) {
                int type = buffer.getShort(entryOffset + 2) & 0xFFFF;
                int count = buffer.getInt(entryOffset + 4);
                if (type != 3 || count != 1) {
                    return 1; // unexpected layout for the orientation tag
                }
                // SHORT with count 1 is stored inline in the first 2 bytes of the value field
                int value = buffer.getShort(entryOffset + 8) & 0xFFFF;
                return value >= 1 && value <= 8 ? value : 1;
            }
        }
        return 1;
    }

    private static void skipFully(DataInputStream in, int bytes) throws IOException {
        int remaining = bytes;
        while (remaining > 0) {
            int skipped = in.skipBytes(remaining);
            if (skipped <= 0) {
                if (in.read() == -1) {
                    throw new EOFException();
                }
                skipped = 1;
            }
            remaining -= skipped;
        }
    }
}
