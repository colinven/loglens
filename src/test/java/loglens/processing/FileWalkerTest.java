package loglens.processing;

import loglens.processing.FileWalker;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.Method;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class FileWalkerTest {

    FileWalker fw = new FileWalker();

    Method reflectIsGzipStream() {
        Method method;
        try {
            method = FileWalker.class.getDeclaredMethod("isGzipStream", BufferedInputStream.class);
            method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return method;
    }

    /* ————— isGzipStream() ————— */
    @Test
    void givenValidGzipHeader_whenIsGzipStream_thenReturnsTrue() throws Exception {
        byte[] data = {(byte) 0x1F, (byte) 0x8B, 0x00, 0x00};
        try (BufferedInputStream gzipStream = new BufferedInputStream(new ByteArrayInputStream(data))) {

            Method isGzipStream = reflectIsGzipStream();
            boolean result = (boolean) isGzipStream.invoke(fw, gzipStream);

            assertThat(result).isTrue();

            // Verify that stream reset properly after return
            assertThat(gzipStream.read()).isEqualTo(0x1F);
            assertThat(gzipStream.read()).isEqualTo(0x8B);
        }
    }

    @Test
    void givenInvalidGzipHeader_whenIsGzipStream_thenReturnsFalse() throws Exception {
        byte[] data = {(byte) 0x8B, (byte) 0x1F, 0x00, 0x00};
        try (BufferedInputStream gzipStream = new BufferedInputStream(new ByteArrayInputStream(data))) {

            Method isGzipStream = reflectIsGzipStream();
            boolean result = (boolean) isGzipStream.invoke(fw, gzipStream);

            assertThat(result).isFalse();

            // Verify that stream reset properly after return
            assertThat(gzipStream.read()).isEqualTo(0x8B);
            assertThat(gzipStream.read()).isEqualTo(0x1F);
        }
    }

    @Test
    void givenEmptyStream_whenIsGzipStream_thenReturnsFalse() throws Exception {
        byte[] data = {};
        try (BufferedInputStream gzipStream = new BufferedInputStream(new ByteArrayInputStream(data))) {

            Method isGzipStream = reflectIsGzipStream();
            boolean result = (boolean) isGzipStream.invoke(fw, gzipStream);

            assertThat(result).isFalse();
        }
    }

    @Test
    void givenOneByteStream_whenIsGzipStream_thenReturnsFalse() throws Exception {
        byte[] data = {(byte) 0x8B};
        try (BufferedInputStream gzipStream = new BufferedInputStream(new ByteArrayInputStream(data))) {

            Method isGzipStream = reflectIsGzipStream();
            boolean result = (boolean) isGzipStream.invoke(fw, gzipStream);

            assertThat(result).isFalse();
        }
    }
}
