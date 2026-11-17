package kh.edu.istad.platform.restapi.service;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

@Component
public class ProfilePictureImageValidator {
    public static final int MAX_BYTES = 5 * 1024 * 1024;
    private static final long MAX_PIXELS = 16_000_000L;
    private static final int MAX_DIMENSION = 8_192;

    public ImageFormat validate(byte[] content, String declaredMediaType) {
        if (content == null || content.length == 0) throw new IllegalArgumentException("Choose an image to upload.");
        if (content.length > MAX_BYTES) throw new IllegalArgumentException("Image must be 5 MB or smaller.");
        String declared = declaredMediaType == null ? "" : declaredMediaType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        ImageFormat expected = ImageFormat.fromMediaType(declared);
        if (expected == null) throw new IllegalArgumentException("Only JPEG, PNG, and WebP images are supported.");

        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(content))) {
            if (input == null) throw invalidImage();
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw invalidImage();
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                ImageFormat actual = ImageFormat.fromReaderName(reader.getFormatName());
                if (actual == null || !actual.equals(expected)) throw new IllegalArgumentException("Image content does not match its file type.");
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width < 1 || height < 1 || width > MAX_DIMENSION || height > MAX_DIMENSION
                        || (long) width * height > MAX_PIXELS) {
                    throw new IllegalArgumentException("Image dimensions are too large.");
                }
                if (reader.read(0) == null) throw invalidImage();
                return actual;
            } finally {
                reader.dispose();
            }
        } catch (IOException | RuntimeException exception) {
            if (exception instanceof IllegalArgumentException illegalArgumentException) throw illegalArgumentException;
            throw new IllegalArgumentException("The uploaded file is not a valid image.", exception);
        }
    }

    private IllegalArgumentException invalidImage() {
        return new IllegalArgumentException("The uploaded file is not a valid image.");
    }

    public record ImageFormat(String extension, String mediaType) {
        private static ImageFormat fromMediaType(String value) {
            return switch (value) {
                case "image/jpeg" -> new ImageFormat("jpg", "image/jpeg");
                case "image/png" -> new ImageFormat("png", "image/png");
                case "image/webp" -> new ImageFormat("webp", "image/webp");
                default -> null;
            };
        }

        private static ImageFormat fromReaderName(String value) {
            return switch (value.toLowerCase(Locale.ROOT)) {
                case "jpeg", "jpg" -> new ImageFormat("jpg", "image/jpeg");
                case "png" -> new ImageFormat("png", "image/png");
                case "webp" -> new ImageFormat("webp", "image/webp");
                default -> null;
            };
        }
    }
}
