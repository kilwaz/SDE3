package sde.application.data.model.links;

import sde.application.data.model.DatabaseLink;
import sde.application.node.objects.SDEFile;

import java.io.File;

public class SDEFileDatabaseLink extends DatabaseLink {
    public SDEFileDatabaseLink() {
        super("serialized", SDEFile.class);

        // Make sure the order is the same as column order in database
        link("uuid", method("getUuidString"), method("setUuidFromString", String.class)); // 1
        link("node_id", method("getParentUuid"), null); // 2
        linkBlob("serial_object", method("getInputStream"), method("setFile", File.class)); // 2
        //link("serial_reference", SDEFile.class.getMethod("keystore")); // 2
    }
}
