package dev.rykrax.rkverse.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ImageValidator {
     // kiểm tra 12 bytes đầu của file xem có đúng định dạng RIFF....WEBP hay không
    public static boolean isWebP(File file) {
        if (file == null || !file.exists() || file.length() < 12) {
            return false;
        }

        byte[] header = new byte[12];
        try (FileInputStream fis = new FileInputStream(file)) {
            int read = fis.read(header);
            if (read < 12) return false;

            // RIFF
            boolean isRiff = header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F';
            // WEBP
            boolean isWebp = header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';

            return isRiff && isWebp;
        } catch (IOException e) {
            return false;
        }
    }
}
