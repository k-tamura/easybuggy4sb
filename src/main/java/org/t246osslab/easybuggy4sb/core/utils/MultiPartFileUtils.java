package org.t246osslab.easybuggy4sb.core.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

/**
 * Utility class to handle multi part files.
 */
public final class MultiPartFileUtils {

    private static final Logger log = LoggerFactory.getLogger(MultiPartFileUtils.class);

    // squid:S1118: Utility classes should not have public constructors
    private MultiPartFileUtils() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * Write uploaded file to the given path.
     *
     * @param savePath Path to save an uploaded file.
     * @param filePart A part or form item that was received within a <code>multipart/form-data</code> POST request.
     * @param fileName The uploaded file name.
     */
    public static boolean writeFile(String savePath, MultipartFile filePart, String fileName) throws IOException {
        boolean isConverted = false;
        try (OutputStream out = new FileOutputStream(savePath + File.separator + fileName);
                InputStream in = filePart.getInputStream()) {
            int read;
            final byte[] bytes = new byte[1024];
            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
        } catch (FileNotFoundException e) {
            // Ignore because file already exists
            log.debug("Exception occurs: ", e);
            isConverted = true;
        }
        return isConverted;
    }
}
