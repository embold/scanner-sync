package io.embold.scan;

import io.embold.scan.exec.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * This is an entry point class to do a modular (or sharded) downloaded. This class works with the backend to download only specific modules as they are needed rather than the entire scanner package.
 * For example, to run a Java scan, you only need the CORE and JAVA packages, and this class supports downloading just that.
 * It accepts a ModularSyncOpts where you specify which packages are needed (CORE is always downloaded regardless of if you specify or not)
 */
public class ModularSyncSession {
    private static Logger logger = LogManager.getLogger(ModularSyncSession.class);
    private ModularSyncOpts opts;

    public ModularSyncSession(ModularSyncOpts opts) {
        this.opts = opts;
    }

    public void run() throws SyncException {
        // Check and download each package
        // Core is always required
        try {
            syncPackage(Package.CORE);
            for (Package pack : opts.getPackages()) {
                syncPackage(pack);
            }
        } catch (Exception e) {
            // Sync failed, delete the package cache, so that the next run will start with a clean state
            FileUtil.deleteDirQuietly(opts.getCoronaPackageCache());
            if (e instanceof SyncException) {
                throw e;
            } else {
                throw new SyncException(e);
            }
        }
    }

    private void syncPackage(Package pack) throws SyncException {
        logger.info("Synching package: {}", pack);
        String checksum = packageChecksum(pack);

        OsCheck.OSType os = OsCheck.getOperatingSystemType();
        if (os.equals(OsCheck.OSType.MacOS)) {
            // Currently if OS is Mac, default to Linux (TODO until we have Mac build)
            os = OsCheck.OSType.Linux;
        }

        File packageFile = Downloader.getShardedPackage(opts, os, pack.name(), checksum, packageFile(pack));

        if (packageFile != null) {
            // A new package was downloaded, so now we need to extract it and replace it at the target path
            File d = new File(packageDir(pack));
            // Ensure clean dir
            FileUtil.deleteDirQuietly(d);
            // Unzip
            Extractor.tgzExtract(packageFile, d);
            // Copy to destination
            PackageMover.run(opts, d);
        }

        logger.info("Finished synching package: {}", pack);
    }

    private String packageChecksum(Package pack) throws SyncException {
        String checksum = null;
        File checksumFile = new File(packageChecksumFile(pack));
        if (checksumFile.exists()) {
            try {
                checksum = StringUtils.strip(FileUtils.readFileToString(checksumFile, Charset.defaultCharset()));
            } catch (IOException e) {
                throw new SyncException(e);
            }
        }

        return checksum;
    }

    private String packageChecksumFile(Package pack) throws SyncException {
        return packageDir(pack) + File.separator + "checksum";
    }

    private String packageDir(Package pack) {
        return opts.getCoronaPackageCache() + File.separator + pack.name();
    }

    private String packageFile(Package pack) {
        return opts.getCoronaPackageCache() + File.separator + pack.name() + ".tar.gz";
    }
}
