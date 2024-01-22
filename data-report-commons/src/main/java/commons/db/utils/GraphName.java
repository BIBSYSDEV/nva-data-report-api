package commons.db.utils;

import java.net.URI;
import nva.commons.core.paths.UnixPath;
import nva.commons.core.paths.UriWrapper;

public final class GraphName {

    public static final char EXTENSION_DELIMITER = '.';
    public static final String NT_EXTENSION = ".nt";
    private final UnixPath unixPath;
    private final String host;

    private GraphName(String host, UnixPath unixPath) {
        this.host = host;
        this.unixPath = unixPath;
    }

    public URI toUri() {
        var type = getType(unixPath);
        var name = getName(unixPath);
        return UriWrapper.fromHost(host)
                   .addChild(type, name)
                   .getUri();
    }

    public static GraphNameBuilder newBuilder() {
        return new GraphNameBuilder();
    }

    private static String getType(UnixPath unixPath) {
        return unixPath.getParent().orElseThrow().getLastPathElement();
    }

    private static String getName(UnixPath unixPath) {
        var name = unixPath.getLastPathElement();
        return name.substring(0, name.lastIndexOf(EXTENSION_DELIMITER)) + NT_EXTENSION;
    }

    public static class GraphNameBuilder {

        private String host;
        private UnixPath unixPath;

        public GraphNameBuilder withBase(String host) {
            this.host = host;
            return this;
        }

        public GraphNameBuilder fromUnixPath(UnixPath unixPath) {
            this.unixPath = unixPath;
            return this;
        }

        public GraphName build() {
            return new GraphName(host, unixPath);
        }
    }
}
