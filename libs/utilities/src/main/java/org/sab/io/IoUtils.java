package org.sab.io;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.base64.Base64;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static io.netty.buffer.Unpooled.copiedBuffer;

public class IoUtils {

    public static String encodeFile(ByteBuf byteBuf) {
        return Base64.encode(byteBuf).toString(StandardCharsets.UTF_8);
    }

    public static String encodeFile(InputStream inputStream) throws IOException {
        byte[] bytes = inputStream.readAllBytes();
        return encodeFile(copiedBuffer(bytes));
    }

    public static InputStream decodeFile(String encodedFile) {
        byte[] bytes = org.apache.commons.codec.binary.Base64.decodeBase64(encodedFile);
        InputStream stream = new ByteArrayInputStream(bytes);
        return stream;
    }
}
