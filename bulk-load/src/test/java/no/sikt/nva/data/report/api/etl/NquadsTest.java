package no.sikt.nva.data.report.api.etl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.stream.Collectors;
import no.sikt.nva.data.report.api.etl.transformer.Nquads;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NquadsTest {

    public static final URI GRAPH_BASE = URI.create("https://example.org/nva");
    @TempDir
    private File directory;

    @Test
    void shouldTransformInputToNquads() throws IOException {
        var file = createFile();
        var actual = Nquads.transform(file, GRAPH_BASE);
        var expected = createExpected(file);
        assertEquals(expected, actual.toString());
    }

    private static String createExpected(File file) throws IOException {
        return Files.readAllLines(file.toPath()).stream()
                   .map(line -> line.replaceAll(" \\.$", ""))
                   .map(line -> line + " <" + GRAPH_BASE + "/" + file.getName() + "> .")
                   .collect(Collectors.joining(System.lineSeparator()))
               + System.lineSeparator();
    }

    private File createFile() throws IOException {
        var data = """
            <https://example.org/s> <https://example.org/p> <https://example.org/o> .
            """;
        var file = new File(directory, "expectedFilename.gz");
        Files.writeString(file.toPath(), data);
        return file;
    }
}
