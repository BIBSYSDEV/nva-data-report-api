package commons;

import nva.commons.core.paths.UnixPath;

public interface StorageWriter {

    void writeCsv(UnixPath location, String content);
}
