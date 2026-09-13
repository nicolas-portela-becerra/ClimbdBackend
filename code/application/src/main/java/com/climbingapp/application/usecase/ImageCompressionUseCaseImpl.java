package com.climbingapp.application.usecase;

import com.climbingapp.domain.usecase.ImageCompressionUseCase;

import org.springframework.stereotype.Service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

/*AI Generated code, image pipeline being rebuilt manually*/
@Service
public class ImageCompressionUseCaseImpl implements ImageCompressionUseCase {

    private static final float JPEG_QUALITY = 0.85f;

    @Override
    public byte[] compress(byte[] imageData, int maxPx) {
        if (maxPx <= 0) {
            throw new IllegalArgumentException("maxPx must be positive");
        }
        BufferedImage source = decodeAndNormalize(imageData);
        double scale =
                Math.min(1.0, (double) maxPx / Math.max(source.getWidth(), source.getHeight()));
        return writeJpeg(render(source, scale));
    }

    @Override
    public byte[] createThumbnail(byte[] imageData, int width) {
        if (width <= 0) {
            throw new IllegalArgumentException("width must be positive");
        }
        BufferedImage source = decodeAndNormalize(imageData);
        double scale = Math.min(1.0, (double) width / source.getWidth());
        return writeJpeg(render(source, scale));
    }

    /**
     * Decodes the image and bakes the EXIF orientation into the pixels, so the resulting JPEG
     * displays correctly without relying on metadata (which the re-render drops).
     */
    private BufferedImage decodeAndNormalize(byte[] imageData) {
        BufferedImage decoded = decode(imageData);
        return applyOrientation(decoded, ExifOrientation.read(imageData));
    }

    private BufferedImage applyOrientation(BufferedImage source, int orientation) {
        if (orientation < 2 || orientation > 8) {
            return source;
        }
        int width = source.getWidth();
        int height = source.getHeight();
        // Each transform maps source pixels to their display-correct position.
        AffineTransform transform =
                switch (orientation) {
                    case 2 -> new AffineTransform(-1, 0, 0, 1, width, 0);
                    case 3 -> new AffineTransform(-1, 0, 0, -1, width, height);
                    case 4 -> new AffineTransform(1, 0, 0, -1, 0, height);
                    case 5 -> new AffineTransform(0, 1, 1, 0, 0, 0);
                    case 6 -> new AffineTransform(0, 1, -1, 0, height, 0);
                    case 7 -> new AffineTransform(0, -1, -1, 0, height, width);
                    case 8 -> new AffineTransform(0, -1, 1, 0, 0, width);
                    default -> throw new IllegalStateException("Unexpected orientation");
                };
        boolean swapDimensions = orientation >= 5;
        BufferedImage target =
                new BufferedImage(
                        swapDimensions ? height : width,
                        swapDimensions ? width : height,
                        BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = target.createGraphics();
        try {
            graphics2D.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics2D.setColor(Color.WHITE);
            graphics2D.fillRect(0, 0, target.getWidth(), target.getHeight());
            graphics2D.drawImage(source, transform, null);
        } finally {
            graphics2D.dispose();
        }
        return target;
    }

    private BufferedImage decode(byte[] imageData) {
        if (imageData == null || imageData.length == 0) {
            throw new IllegalArgumentException("Image data must not be empty");
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
            if (image == null) {
                throw new IllegalArgumentException(
                        "Unsupported image format or corrupt image data");
            }
            return image;
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read image data", e);
        }
    }

    private BufferedImage render(BufferedImage source, double scale) {
        int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = target.createGraphics();
        try {
            graphics2D.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics2D.setRenderingHint(
                    RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics2D.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setColor(Color.WHITE);
            graphics2D.fillRect(0, 0, width, height);
            graphics2D.drawImage(source, 0, 0, width, height, null);
        } finally {
            graphics2D.dispose();
        }
        return target;
    }

    private byte[] writeJpeg(BufferedImage image) {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No JPEG ImageWriter available");
        }
        ImageWriter writer = writers.next();
        try (ByteArrayOutputStream imageOut = new ByteArrayOutputStream();
                ImageOutputStream imgOutStream = ImageIO.createImageOutputStream(imageOut)) {
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(JPEG_QUALITY);
            writer.setOutput(imgOutStream);
            writer.write(null, new IIOImage(image, null, null), param);
            imgOutStream.flush();
            return imageOut.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to write JPEG image", e);
        } finally {
            writer.dispose();
        }
    }
}
