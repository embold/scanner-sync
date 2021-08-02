package io.embold.scan.exec;

import com.google.gson.*;
import io.embold.scan.SyncException;
import io.embold.scan.SyncOpts;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * This class moves directories within a downloaded package to their respective locations in the target location
 */
public class PackageMover {
    private static Logger logger = LogManager.getLogger(PackageMover.class);

    /**
     * @param opts       The sync options
     * @param srcPackDir Location of the downloaded and un-tarred directory (this is where the manifest.json is expected)
     * @throws SyncException
     */
    public static void run(SyncOpts opts, File srcPackDir) throws SyncException {
        try {
            JsonArray modules = JsonParser.parseString(FileUtils.readFileToString(new File(srcPackDir + File.separator + "manifest.json"), Charset.defaultCharset())).getAsJsonArray();
            if (modules != null) {
                for (int i = 0; i < modules.size(); ++i) {
                    JsonObject module = modules.get(i).getAsJsonObject();
                    String name = module.get("name").getAsString();
                    String location = module.get("location").getAsString();
                    String sourceLocation = StringUtils.replace(location, "${ROOT}", srcPackDir.getAbsolutePath());
                    sourceLocation = StringUtils.replace(sourceLocation, "${name}", name);
                    String targetLocation = StringUtils.replace(location, "${ROOT}", opts.getCoronaHome());
                    targetLocation = StringUtils.replace(targetLocation, "${name}", name);

                    File sourceDir = new File(sourceLocation);
                    File targetDir = new File(targetLocation);
                    FileUtil.deleteDirQuietly(targetDir);

                    File parentOfTarget = targetDir.getParentFile();
                    if (!parentOfTarget.exists()) {
                        if (!parentOfTarget.mkdirs()) {
                            throw new SyncException("Could not create destination dir: " + parentOfTarget);
                        }
                    }

                    logger.debug("Moving: {} to: {}", sourceDir, targetDir);
                    FileUtils.moveDirectory(sourceDir, targetDir);
                }
            }
        } catch (IOException e) {
            throw new SyncException(e);
        }
    }
}
