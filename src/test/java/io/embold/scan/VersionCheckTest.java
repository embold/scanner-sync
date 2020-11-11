package io.embold.scan;

import io.embold.scan.exec.Constants;
import io.embold.scan.exec.FileUtil;
import io.embold.scan.exec.VersionCheck;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class VersionCheckTest {
    private static Logger logger = LogManager.getLogger(VersionCheckTest.class);
    private static File tmpCoronaLocation = null;

    private static final String EMB_URL = "";
    private static final String EMB_TOKEN = "";

    @BeforeAll
    public static void setup() throws IOException {
        tmpCoronaLocation = Files.createTempDirectory(null).toFile();
        tmpCoronaLocation.deleteOnExit();
        logger.info("Temp corona location: {}", tmpCoronaLocation);
    }

    @Test
    void versionCheckEmptyCoronaDir() {
        File coronaDir = new File(tmpCoronaLocation + File.separator + Constants.CORONA);

        try {
            SyncOpts opts = new SyncOpts(EMB_URL, EMB_TOKEN, tmpCoronaLocation.getAbsolutePath());
            String checksum = VersionCheck.run(opts);
            assertNull(checksum);
        } finally {
            FileUtil.deleteDirQuietly(coronaDir);
        }
    }

    @Test
    void versionCheckWithCoronaDirInvalidPropsFileName() {
        File coronaDir = new File(tmpCoronaLocation + File.separator + Constants.CORONA);
        try {
            FileUtils.touch(new File(coronaDir + File.separator + "x.properties"));
            SyncOpts opts = new SyncOpts(EMB_URL, EMB_TOKEN, tmpCoronaLocation.getAbsolutePath());
            String checksum = VersionCheck.run(opts);
            assertNull(checksum);
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            FileUtil.deleteDirQuietly(coronaDir);
        }
    }

    @Test
    void versionCheckWithCoronaDirPropsFileInvalidContent() {
        File coronaDir = new File(tmpCoronaLocation + File.separator + Constants.CORONA);
        try {
            File f = new File(coronaDir + File.separator + "buildNumber-1.properties");
            FileUtils.write(f, "invalidprops", Charset.defaultCharset());
            SyncOpts opts = new SyncOpts(EMB_URL, EMB_TOKEN, tmpCoronaLocation.getAbsolutePath());
            String checksum = VersionCheck.run(opts);
            assertNull(checksum);
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            FileUtil.deleteDirQuietly(coronaDir);
        }
    }

    @Test
    void versionCheckWithCoronaDirValidPropsFile() {
        File coronaDir = new File(tmpCoronaLocation + File.separator + Constants.CORONA);
        try {
            File f = new File(coronaDir + File.separator + "buildNumber-1.properties");
            FileUtils.write(f, "Version: 20.7.1-SNAPSHOT\n", Charset.defaultCharset(), true);
            FileUtils.write(f, "CommitID: 8b5aca4a33386aaef99877b020392e7dae3931a6\n", Charset.defaultCharset(), true);
            FileUtils.write(f, "Timestamp: 2020-09-16 15:28:40 +0530\n", Charset.defaultCharset(), true);
            SyncOpts opts = new SyncOpts(EMB_URL, EMB_TOKEN, tmpCoronaLocation.getAbsolutePath());
            String checksum = VersionCheck.run(opts);
            assertNotNull(checksum);
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            FileUtil.deleteDirQuietly(coronaDir);
        }
    }

    @AfterAll
    public static void tearDown() {
        logger.info("Deleting temp corona location: {}", tmpCoronaLocation);
        FileUtil.deleteDirQuietly(tmpCoronaLocation);
    }
}
