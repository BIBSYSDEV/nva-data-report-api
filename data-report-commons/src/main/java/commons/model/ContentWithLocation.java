package commons.model;

import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UnixPath;

@JacocoGenerated //Used in abstract class BulkTransformerHandler, subclasses tested in modules bulk-export and bulk-load
public record ContentWithLocation(UnixPath location, String content) {

}
