package org.sab.classes;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public static Path getResourcePath(String resource) {
        final URL url = Reader.class.getClassLoader().getResource(resource);

        if (url == null) {
            return null;
        }

        try {
            return Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public static byte[] readBytesFromResource(String resource) throws IOException {
        final Path path = getResourcePath(resource);
        if(path == null) {
            throw new IOException("Path could not be found.");
        }

        return Reader.getBytes(path);
    }
}
