package io.embold.scan;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SyncSessionTest extends PreReqBase {
    private static final Logger logger = LogManager.getLogger(SyncSessionTest.class);

    @Test
    void shouldSyncClean() {
        try {
            SyncOpts opts = new SyncOpts(TestConstants.EMB_URL, TestConstants.EMB_TOKEN,
                    tmpCoronaLocation.getAbsolutePath());
            new SyncSession(opts).run();
        } catch (SyncException e) {
            fail(e.getMessage());
        }
    }
}
