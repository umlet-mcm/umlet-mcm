package at.ac.tuwien.model.change.management.graphdb.utils;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.*;

/**
 * Utility class for database
 */
public class Utils {
    /**
     * Copy a file from a jar to a file
     *
     * @param source The source file
     * @param target The target file
     * @throws IOException If an error occurs
     */
    public static void copyFromJar(ClassPathResource source, final File target) throws IOException {
        // Create parent directories
        FileUtils.createParentDirectories(target);

        // Copy the file
        try (InputStream inputStream = source.getInputStream(); OutputStream outputStream = new FileOutputStream(target)) {

            byte[] buf = new byte[1024];

            int bytesRead;

            while ((bytesRead = inputStream.read(buf)) > 0) {

                outputStream.write(buf, 0, bytesRead);

            }

        }
    }
}

