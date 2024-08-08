package commons.utils;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.Test;

class GzipUtilTest {

    private static final int ONE_KILOBYTE = 1024;
    private static final int EOS = -1;
    private static final int ZERO_OFFSET = 0;

    @Test
    void shouldCompress() throws IOException {
        var input = "Some data to compress";
        var compressedData = GzipUtil.compress(input);
        assertNotNull(compressedData);
        assertTrue(compressedData.length > 0);
        var decompressedData = decompress(compressedData);
        assertEquals(decompressedData, input);
    }

    @Test
    void shouldCompressEmptyString() throws IOException {
        var uncompressedData = "";
        var compressedData = GzipUtil.compress(uncompressedData);
        assertNotNull(compressedData);
        assertTrue(compressedData.length > 0);
    }

    private static String decompress(byte[] compressedData) throws IOException {
        var inputStream = new ByteArrayInputStream(compressedData);
        var outputStream = new ByteArrayOutputStream();
        try (var gzip = new GZIPInputStream(inputStream)) {
            var buffer = new byte[ONE_KILOBYTE];
            int len;
            while ((len = gzip.read(buffer)) != EOS) {
                outputStream.write(buffer, ZERO_OFFSET, len);
            }
        }
        return outputStream.toString(UTF_8);
    }
}