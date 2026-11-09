package com.climbingapp.application.usecase;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

import javax.imageio.ImageIO;

class ImageProcessorUseCaseImplTest {

    private final ImageProcessorUseCaseImpl processor = new ImageProcessorUseCaseImpl();

    @Test
    void rejectsImageDeclaringHugeRasterBeforeDecoding() {
        byte[] bomb = pngHeader(30_000, 30_000);

        assertThrows(IllegalArgumentException.class, () -> processor.compress(bomb, 1000));
        assertThrows(IllegalArgumentException.class, () -> processor.createThumbnail(bomb, 100));
        assertThrows(IllegalArgumentException.class, () -> processor.resize(bomb, 100, 100));
    }

    @Test
    void rejectsUnknownFormat() {
        assertThrows(
                IllegalArgumentException.class,
                () -> processor.compress("not an image".getBytes(), 1000));
    }

    @Test
    void acceptsNormalImage() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(20, 10, BufferedImage.TYPE_INT_RGB), "png", out);

        assertDoesNotThrow(() -> processor.compress(out.toByteArray(), 1000));
    }

    /** PNG signature + IHDR only: claims the given size without any pixel data. */
    private static byte[] pngHeader(int width, int height) {
        ByteBuffer ihdr = ByteBuffer.allocate(17);
        ihdr.put("IHDR".getBytes()).putInt(width).putInt(height);
        ihdr.put(new byte[] {8, 2, 0, 0, 0}); // 8-bit RGB
        CRC32 crc = new CRC32();
        crc.update(ihdr.array());
        return ByteBuffer.allocate(8 + 4 + 17 + 4)
                .put(new byte[] {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n'})
                .putInt(13)
                .put(ihdr.array())
                .putInt((int) crc.getValue())
                .array();
    }
}
