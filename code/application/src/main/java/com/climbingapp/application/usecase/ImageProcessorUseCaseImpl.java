package com.climbingapp.application.usecase;

import com.climbingapp.domain.usecase.ImageProcessorUseCase;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.ScaleMethod;
import com.sksamuel.scrimage.metadata.ImageMetadata;
import com.sksamuel.scrimage.metadata.Tag;
import com.sksamuel.scrimage.nio.ImageWriter;
import com.sksamuel.scrimage.nio.JpegWriter;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@Service
public class ImageProcessorUseCaseImpl implements ImageProcessorUseCase {

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
        ImmutableImage compressedSource = source.scale(scale, ScaleMethod.FastScale);

        ImageWriter writer = new JpegWriter(50, true);
        byte[] processedImage = null;
        try {
            processedImage = compressedSource.bytes(writer);
        } catch (IOException e) {
            throw new RuntimeException("Error while compressing image", e);
        }
        return processedImage;
    }

    private ImmutableImage readBytes(byte[] imageData) {
        try {
            return ImmutableImage.loader().fromBytes(imageData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
