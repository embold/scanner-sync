package io.embold.scan;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.fail;

class ModularSyncSessionTest extends PreReqBase {
    private static Logger logger = LogManager.getLogger(ModularSyncSessionTest.class);

    @Test
    void shouldSyncClean() {
        Set<Package> packs = new LinkedHashSet<>();
        packs.add(Package.JAVA);
        try {
            ModularSyncOpts opts = new ModularSyncOpts(TestConstants.EMB_URL + "/shardedpackagedownload/", TestConstants.EMB_TOKEN, tmpCoronaLocation.getAbsolutePath(), packs);
            new ModularSyncSession(opts).run();
        } catch (SyncException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void shouldSyncCleanAndCached() {
        Set<Package> packs = new LinkedHashSet<>();
        packs.add(Package.JAVA);
        try {
            ModularSyncOpts opts = new ModularSyncOpts(TestConstants.EMB_URL + "/shardedpackagedownload/", TestConstants.EMB_TOKEN, tmpCoronaLocation.getAbsolutePath(), packs);
            ModularSyncSession session = new ModularSyncSession(opts);
            session.run();
            session.run();

        } catch (SyncException e) {
            fail(e.getMessage());
        }
    }
}
