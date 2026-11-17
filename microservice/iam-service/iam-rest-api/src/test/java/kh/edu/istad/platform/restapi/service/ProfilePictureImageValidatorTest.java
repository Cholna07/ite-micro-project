package kh.edu.istad.platform.restapi.service;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class ProfilePictureImageValidatorTest {
    private final ProfilePictureImageValidator validator = new ProfilePictureImageValidator();

    @Test
    void acceptsAndDecodesJpegPngAndWebpContent() throws Exception {
        assertEquals("image/png", validator.validate(image("png"), "image/png").mediaType());
        assertEquals("image/jpeg", validator.validate(image("jpeg"), "image/jpeg").mediaType());
        byte[] webp = Base64.getDecoder().decode("UklGRiIAAABXRUJQVlA4IBYAAAAwAQCdASoBAAEALmk0mk0iIiIiIgBoSygABc6zbAAA");
        assertEquals("image/webp", validator.validate(webp, "image/webp").mediaType());
    }

    @Test
    void rejectsUnsupportedTypesAndContentThatDoesNotMatchMimeType() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> validator.validate(image("png"), "image/gif"));
        assertThrows(IllegalArgumentException.class, () -> validator.validate(image("png"), "image/jpeg"));
        assertThrows(IllegalArgumentException.class, () -> validator.validate(new byte[]{1, 2, 3}, "image/png"));
    }

    @Test
    void rejectsImagesLargerThanFiveMegabytes() {
        assertThrows(IllegalArgumentException.class, () -> validator.validate(
                new byte[ProfilePictureImageValidator.MAX_BYTES + 1], "image/png"));
    }

    private byte[] image(String format) throws Exception {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(image, format, output));
        return output.toByteArray();
    }
}
