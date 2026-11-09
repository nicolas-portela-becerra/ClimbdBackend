package com.climbingapp.application.usecase;

import com.climbingapp.domain.usecase.ImageProcessorUseCase;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.ScaleMethod;
import com.sksamuel.scrimage.metadata.ImageMetadata;
import com.sksamuel.scrimage.metadata.Tag;
import com.sksamuel.scrimage.nio.ImageWriter;
import com.sksamuel.scrimage.nio.JpegWriter;
import com.sksamuel.scrimage.nio.PngWriter;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

@Slf4j
@Service
public class ImageProcessorUseCaseImpl implements ImageProcessorUseCase {

    static final int MAX_SIDE_PX = 8000;

    static final long MAX_PIXELS = 40000000L;

    @Override
    public byte[] compress(byte[] imageData, int maxPx) {
        if (maxPx <= 0) {
            throw new IllegalArgumentException("maxPx must be positive");
        }
        // Read the image
        ImmutableImage source = readBytes(imageData);

        // Get the type of the image
        int imageType = source.getType();
        log.info("Image type: {}", imageType);

        // Get image metadata
        ImageMetadata metadata = source.getMetadata();
        Tag[] tags = metadata.tags();
        log.info("Image metadata: {}", metadata);
        log.info("Image tags: {}", Arrays.toString(tags));

        // Scale image to different dimensions
        double scale = Math.min(1.0, (double) maxPx / Math.max(source.width, source.height));
        ImmutableImage compressedSource = source.scale(scale, ScaleMethod.FastScale);

        // Create writer to compress and write to bytes the image
        ImageWriter writer = new JpegWriter(70, true);
        byte[] processedImage = null;
        try {
            processedImage = compressedSource.bytes(writer);
        } catch (IOException e) {
            throw new RuntimeException("Error while compressing image", e);
        }

        // Return the image after processing
        return processedImage;
    }

    @Override
    public byte[] createThumbnail(byte[] imageData, int width) {
        if (width <= 0) {
            throw new IllegalArgumentException("width must be positive");
        }
        ImmutableImage source = readBytes(imageData);
        double scale = Math.min(1.0, (double) width / source.width);
        ImmutableImage compressedSource = source.scale(scale, ScaleMethod.Bicubic);

        ImageWriter writer = new JpegWriter(70, true);
        byte[] processedImage = null;
        try {
            processedImage = compressedSource.bytes(writer);
        } catch (IOException e) {
            throw new RuntimeException("Error while creating thumbnail", e);
        }
        return processedImage;
    }

    @Override
    public byte[] resize(byte[] imageData, int width, int height) {
        ImmutableImage source = readBytes(imageData);
        ImmutableImage resized = source.bound(width, height);
        ImageWriter writer =
                resized.hasAlpha() ? PngWriter.MaxCompression : new JpegWriter(80, true);

        byte[] processedImage = null;
        try {
            processedImage = resized.bytes(writer);
        } catch (IOException e) {
            throw new RuntimeException("Error while resizing image", e);
        }
        return processedImage;
    }

    private ImmutableImage readBytes(byte[] imageData) {
        checkDimensions(imageData);
        try {
            return ImmutableImage.loader().fromBytes(imageData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads only the image header so a small file declaring a huge raster (decompression bomb) is
     * rejected before the pixels are decoded into memory.
     */
    static void checkDimensions(byte[] imageData) {
        try (ImageInputStream in =
                ImageIO.createImageInputStream(new ByteArrayInputStream(imageData))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (!readers.hasNext()) {
                throw new IllegalArgumentException("Unsupported image format");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(in, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width > MAX_SIDE_PX
                        || height > MAX_SIDE_PX
                        || (long) width * height > MAX_PIXELS) {
                    throw new IllegalArgumentException(
                            "Image dimensions " + width + "x" + height + " exceed the limit");
                }
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Unreadable image", e);
        }
    }
}
