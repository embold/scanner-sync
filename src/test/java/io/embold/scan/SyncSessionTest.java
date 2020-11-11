package io.embold.scan;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SyncSessionTest extends PreReqBase {
    private static Logger logger = LogManager.getLogger(SyncSessionTest.class);

    @Test
    void shouldSyncClean() {
        SyncOpts opts = new SyncOpts(TestConstants.EMB_URL, TestConstants.EMB_TOKEN, tmpCoronaLocation.getAbsolutePath());
        try {
            new SyncSession(opts).run();
        } catch (SyncException e) {
            fail(e.getMessage());
        }
    }
}
