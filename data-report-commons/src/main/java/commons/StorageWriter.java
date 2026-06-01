package commons;

import nva.commons.core.paths.UnixPath;

@FunctionalInterface
public interface StorageWriter {

    void writeCsv(UnixPath location, String content);
}
