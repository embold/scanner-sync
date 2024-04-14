package io.embold.scan;

import io.embold.scan.exec.*;
import kong.unirest.Unirest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * This is the main class to run a sync session which downloads corona package from the Embold Server,
 * un-tars it and copies it to the desired location, if the current version is not in sync with the server
 */
public class SyncSession {
    private static final Logger logger = LogManager.getLogger(SyncSession.class);
    private final SyncOpts opts;

    public SyncSession(SyncOpts opts) {
        this.opts = opts;
    }

    public void run() throws SyncException {
        logger.info("Begin sync");
        String checksum = VersionCheck.run(this.opts);
        if (StringUtils.isEmpty(checksum)) {
            logger.info("No checksum found, downloading packages");
        } else {
            logger.info("Current package checksum: {}, checking for updates", checksum);
        }

        File tmpCoronaLocation = null;
        try {
            tmpCoronaLocation = getTempDir();
            logger.debug("Temporary root location: {}", tmpCoronaLocation);
            OsCheck.OSType os = OsCheck.getOperatingSystemType();
            if (os.equals(OsCheck.OSType.MacOS)) {
                // Currently if OS is Mac, default to Linux (TODO until we have Mac build)
                os = OsCheck.OSType.Linux;
            }
            String tmpCoronaArchive = tmpCoronaLocation.getAbsolutePath() + File.separator + Constants.CORONA_ARCHIVE;
            boolean downloaded = Downloader.getCoronaPackage(opts, os, checksum, tmpCoronaArchive);
            if (downloaded) {
                // A new package was downloaded, so now we need to extract it and replace it at the target path

                File destLocation = new File(opts.getCoronaHome());
                File origLocation = new File(opts.getCoronaLocation() + File.separator + "corona_orig");

                FileUtil.deleteDirQuietly(origLocation);

                // First backup the existing (orig) package by renaming
                if (destLocation.exists()) {
                    logger.debug("Destination: {} exists", destLocation);
                    logger.debug("Backup {} to {}", destLocation, origLocation);
                    try {
                        FileUtils.moveDirectory(destLocation, origLocation);
                    } catch (IOException e) {
                        logger.error("Could not backup existing embold scanner packages", e);
                        throw new SyncException(e);
                    }
                }

                // First extract it to the dest location
                try {
                    // Ensure clean dir
                    FileUtil.deleteDirQuietly(new File(opts.getCoronaHome()));
                    Extractor.tgzExtract(new File(tmpCoronaArchive), new File(opts.getCoronaLocation()));
                } catch (SyncException e) {
                    // Restore
                    FileUtil.deleteDirQuietly(destLocation);
                    origLocation.renameTo(destLocation);
                    throw e;
                }

                // Delete the backup
                FileUtil.deleteDirQuietly(origLocation);

                logger.info("Updated embold scanner to checksum: {}", checksum);
            } else {
                // Package is already up-to date. Nothing to be done
                logger.info("Embold scanner package is up-to date");
            }

        } catch (SyncException e) {
            logger.error("Could not update scanner packages", e);
            throw e;
        } finally {
            logger.debug("Delete temporary root location: {}", tmpCoronaLocation);
            FileUtil.deleteDirQuietly(tmpCoronaLocation);
        }

        logger.info("End sync");
    }

    public static void shutdown() {
        Unirest.shutDown();
    }

    private static File getTempDir() throws SyncException {
        try {
            return Files.createTempDirectory(null).toFile();
        } catch (IOException e) {
            logger.error("Error creating temporary location for scanner packages", e);
            throw new SyncException(e);
        }
    }
}
