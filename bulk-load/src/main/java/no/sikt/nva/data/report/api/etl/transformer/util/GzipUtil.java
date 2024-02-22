package no.sikt.nva.data.report.api.etl.transformer.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public final class GzipUtil {

    private static final int ONE_KILOBYTE = 1024;
    private static final int EOS = -1;
    private static final int ZERO_OFFSET = 0;

    private GzipUtil() {
        // NO-OP
    }

    @SuppressWarnings("PMD.AssignementInOperand")
    public static byte[] compress(String uncompressedData) throws IOException {
        var inputStream = new ByteArrayInputStream(uncompressedData.getBytes(UTF_8));
        var outputStream = new ByteArrayOutputStream();

        try (var gzip = new GZIPOutputStream(outputStream)) {
            var buffer = new byte[ONE_KILOBYTE];
            int len;
            while ((len = inputStream.read(buffer)) != EOS) {
                gzip.write(buffer, ZERO_OFFSET, len);
            }
        }
        return outputStream.toByteArray();
    }
}
