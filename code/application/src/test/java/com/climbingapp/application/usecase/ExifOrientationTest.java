package com.climbingapp.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

class ExifOrientationTest {

    @Test
    void readsBigEndianOrientation() {
        assertEquals(6, ExifOrientation.read(jpegWithExif(6, true)));
    }

    @Test
    void readsLittleEndianOrientation() {
        assertEquals(8, ExifOrientation.read(jpegWithExif(8, false)));
    }

    @Test
    void skipsNonExifApp1Segments() {
        byte[] xmp = app1Segment("http://ns.adobe.com/xap/1.0/".getBytes());
        byte[] exifJpeg = jpegWithExif(3, true);
        // SOI + XMP APP1 + (Exif APP1 + EOI from the other fixture)
        byte[] combined = new byte[2 + xmp.length + exifJpeg.length - 2];
        System.arraycopy(exifJpeg, 0, combined, 0, 2);
        System.arraycopy(xmp, 0, combined, 2, xmp.length);
        System.arraycopy(exifJpeg, 2, combined, 2 + xmp.length, exifJpeg.length - 2);
        assertEquals(3, ExifOrientation.read(combined));
    }

    @Test
    void returnsNormalForJpegWithoutExif() {
        assertEquals(1, ExifOrientation.read(new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xD9}));
    }

    @Test
    void returnsNormalForNonJpeg() {
        byte[] pngSignature = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
        assertEquals(1, ExifOrientation.read(pngSignature));
    }

    @Test
    void returnsNormalForGarbage() {
        assertEquals(1, ExifOrientation.read(new byte[] {1, 2, 3}));
    }

    @Test
    void returnsNormalForTruncatedExif() {
        byte[] jpeg = jpegWithExif(6, true);
        byte[] truncated = new byte[16];
        System.arraycopy(jpeg, 0, truncated, 0, truncated.length);
        assertEquals(1, ExifOrientation.read(truncated));
    }

    @Test
    void returnsNormalForOutOfRangeOrientation() {
        assertEquals(1, ExifOrientation.read(jpegWithExif(9, true)));
    }

    private static byte[] jpegWithExif(int orientation, boolean bigEndian) {
        byte[] tiff =
                bigEndian
                        ? new byte[] {
                            'M', 'M', 0, 42, 0, 0, 0, 8,
                            0, 1,
                            0x01, 0x12, 0, 3, 0, 0, 0, 1, 0, (byte) orientation, 0, 0,
                            0, 0, 0, 0
                        }
                        : new byte[] {
                            'I', 'I', 42, 0, 8, 0, 0, 0,
                            1, 0,
                            0x12, 0x01, 3, 0, 1, 0, 0, 0, (byte) orientation, 0, 0, 0,
                            0, 0, 0, 0
                        };
        byte[] exifHeader = {'E', 'x', 'i', 'f', 0, 0};
        byte[] exifPayload = new byte[exifHeader.length + tiff.length];
        System.arraycopy(exifHeader, 0, exifPayload, 0, exifHeader.length);
        System.arraycopy(tiff, 0, exifPayload, exifHeader.length, tiff.length);

        byte[] app1 = app1Segment(exifPayload);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0xFF);
        out.write(0xD8); // SOI
        out.write(app1, 0, app1.length);
        out.write(0xFF);
        out.write(0xD9); // EOI
        return out.toByteArray();
    }

    private static byte[] app1Segment(byte[] content) {
        int length = content.length + 2; // length field includes itself
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0xFF);
        out.write(0xE1); // APP1
        out.write((length >> 8) & 0xFF);
        out.write(length & 0xFF);
        out.write(content, 0, content.length);
        return out.toByteArray();
    }
}
