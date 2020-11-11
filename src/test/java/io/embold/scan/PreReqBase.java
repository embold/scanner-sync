package io.embold.scan;

import io.embold.scan.exec.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public abstract class PreReqBase {
    private static Logger logger = LogManager.getLogger(PreReqBase.class);
    protected static File tmpCoronaLocation = null;

    private static ApiServer srv = null;

    @BeforeAll
    public static void setup() throws IOException {
        tmpCoronaLocation = Files.createTempDirectory(null).toFile();
        tmpCoronaLocation.deleteOnExit();
        logger.info("Temp corona location: {}", tmpCoronaLocation);
        srv = new ApiServer();
        srv.start();
    }

    @AfterAll
    public static void tearDown() {
        logger.info("Deleting temp corona location: {}", tmpCoronaLocation);
        FileUtil.deleteDirQuietly(tmpCoronaLocation);

        SyncSession.shutdown();

        srv.stop();
        srv = null;
    }
}
