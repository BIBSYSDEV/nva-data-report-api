package commons.handlers;

import nva.commons.core.paths.UnixPath;

public record ContentWithLocation(UnixPath location, String content) {

}
