package commons;

import nva.commons.core.paths.UnixPath;

public interface StorageWriter {

    void write(UnixPath location, String content);
}
