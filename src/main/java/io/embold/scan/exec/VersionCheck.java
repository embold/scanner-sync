package io.embold.scan.exec;

import io.embold.scan.SyncOpts;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;

public class VersionCheck {
    private static Logger logger = LogManager.getLogger(VersionCheck.class);

    private VersionCheck(){}

    /**
     * Returns current version checksum, or null if corona was not found at the expected location
     *
     * @return current corona checksum
     */
    public static String run(SyncOpts opts) {
        String checksum = null;
        File coronaDir = new File(opts.getCoronaHome());
        if (coronaDir.exists() && coronaDir.isDirectory()) {
            Collection<File> propsFileList = FileUtils.listFiles(coronaDir, new String[]{"properties"}, false);
            if (CollectionUtils.isNotEmpty(propsFileList)) {
                if (!ensureSinglePropsFile(propsFileList)) {
                    logger.warn("Mismatched build version files, skipping version check");
                } else {
                    File propsFile = propsFileList.iterator().next();
                    String fileName = propsFile.getAbsolutePath();
                    String versionNumber = StringUtils.substringBetween(fileName, "buildNumber-", ".properties");
                    logger.info("Current version of corona: {}", versionNumber);
                    if (validatePropsContent(propsFile)) {
                        try (FileInputStream fis = new FileInputStream(propsFile)) {
                            checksum = DigestUtils.md5Hex(fis);
                            logger.info("Checksum for current version: {}", checksum);
                        } catch (IOException e) {
                            logger.error("Error calculating checksum, skipping version check", e);
                        }
                    } else {
                        logger.error("Invalid version file, skipping version check");
                    }
                }
            } else {
                logger.warn("Scanner version info not found at {}, skipping version check", opts.getCoronaHome());
            }
        } else {
            logger.debug("Scanner not found at {}", opts.getCoronaHome());
        }

        return checksum;
    }

    private static boolean validatePropsContent(File propsFile) {
        boolean result = false;
        try {
            List<String> lines = FileUtils.readLines(propsFile, Charset.defaultCharset());
            result = CollectionUtils.size(lines) == 3 &&
                    StringUtils.startsWith(lines.get(0), "Version:") &&
                    StringUtils.startsWith(lines.get(1), "CommitID:") &&
                    StringUtils.startsWith(lines.get(2), "Timestamp:");

        } catch (IOException e) {
            logger.error("Error reading version file", e);
        }

        return result;
    }

    // check only one build properties file is present
    private static boolean ensureSinglePropsFile(Collection<File> fileList) {
        int count = 0;
        for (File file : fileList) {
            String name = file.getName();
            if (StringUtils.startsWith(name, "buildNumber-")) {
                count++;
            }//end of if
        }//end of for loop
        return count == 1;
    }
}
