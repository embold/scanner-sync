package io.embold.scan.exec;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class FileUtil {
    private FileUtil(){}
    private static Logger logger = LogManager.getLogger(FileUtil.class);

    public static void deleteDirQuietly(String dir) {
        deleteDirQuietly(new File(dir));
    }
    public static void deleteDirQuietly(File dir) {
        if (dir != null) {
            logger.debug("Deleting: {}", dir);
            FileUtils.deleteQuietly(dir);
            logger.debug("Finished deleting: {}", dir);
        }
    }
}
