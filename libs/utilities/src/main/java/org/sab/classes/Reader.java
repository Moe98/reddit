package org.sab.classes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Reader {
    private Reader() {
    }

    public static byte[] getBytes(Path path) throws IOException {
        byte[] buffer;

        try (final InputStream inputStream = Files.newInputStream(path)) {
            buffer = inputStream.readAllBytes();
        }

        return buffer;
    }
}
