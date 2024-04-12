package io.embold.scan;

import io.embold.scan.exec.Constants;
import io.embold.scan.exec.Downloader;
import io.embold.scan.exec.Extractor;
import io.embold.scan.exec.OsCheck;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.junit.jupiter.api.Assertions.*;


import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;

class DownloadTest extends PreReqBase {
    private static Logger logger = LogManager.getLogger(DownloadTest.class);

    @Test
    void shouldSyncClean() {
        SyncOpts opts = new SyncOpts(TestConstants.EMB_URL + "/shardedpackagedownload", TestConstants.EMB_TOKEN, tmpCoronaLocation.getAbsolutePath());
        try {
            String coronaArchive = tmpCoronaLocation.getAbsolutePath() + File.separator + Constants.CORONA_ARCHIVE;
            Downloader.getCoronaPackage(opts, OsCheck.OSType.Linux, null, coronaArchive);
        } catch (SyncException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void shouldDownloadTwice() {
        SyncOpts opts = new SyncOpts(TestConstants.EMB_URL + "/shardedpackagedownload", TestConstants.EMB_TOKEN, tmpCoronaLocation.getAbsolutePath());
        try {
            String coronaArchive = tmpCoronaLocation.getAbsolutePath() + File.separator + Constants.CORONA_ARCHIVE;
            boolean downloaded = Downloader.getCoronaPackage(opts, OsCheck.OSType.Linux, null, coronaArchive);
            assertTrue(downloaded);

            downloaded = Downloader.getCoronaPackage(opts, OsCheck.OSType.Linux, "X-CHECKSUM", coronaArchive);
            assertTrue(downloaded);

            String coronaExtractedLocation = tmpCoronaLocation.getAbsolutePath() + File.separator + "_corona";
            Extractor.tgzExtract(new File(coronaArchive), new File(coronaExtractedLocation));

            Collection<File> propsFileList = FileUtils.listFiles(new File(coronaExtractedLocation + File.separator + Constants.CORONA), new String[]{"properties"}, false);
            File propsFile = propsFileList.iterator().next();

            try (FileInputStream fis = new FileInputStream(propsFile)) {
                String checksum = DigestUtils.md5Hex(fis);
                logger.info("Checksum for current version: {}", checksum);
                downloaded = Downloader.getCoronaPackage(opts, OsCheck.OSType.Linux, "TESTCHECKSUM", coronaArchive);
                assertFalse(downloaded);
            } catch (IOException e) {
                logger.error("Error calculating checksum, skipping version check", e);
            }

        } catch (SyncException e) {
            fail(e.getMessage());
        }
    }
}
