package io.embold.scan.exec;

import io.embold.scan.SyncException;
import io.embold.scan.SyncOpts;
import kong.unirest.GetRequest;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

public class Downloader {
    private static Logger logger = LogManager.getLogger(Downloader.class);

    private Downloader() {
    }

    /**
     * @param opts The sync options
     * @param os The target OS
     * @param checksum The current file checksum. Download is triggered if this doesn't match with the server-side checksum
     * @param destFile Destination file location
     * @return true if package was downloaded (if the checksum didn't match), else false (if the checksum matched)
     * @throws SyncException
     */
    public static boolean getPackage(SyncOpts opts, OsCheck.OSType os, String checksum, String destFile) throws SyncException {
        String url = opts.getEmboldUrl() + "/api/v1/packagedownload/corona?os=" + os.osname();
        if (StringUtils.isNotEmpty("checksum")) {
            url += "&checksum=" + checksum;
        }

        GetRequest request = Unirest.get(url);
        request.getHeaders().add("Authorization", "Bearer " + opts.getEmboldToken());

        File f = new File(destFile);
        if (f.exists()) {
            FileUtils.deleteQuietly(f);
        }

        HttpResponse<File> response = request.asFile(destFile);
        if (response.isSuccess()) {
            if (response.getStatus() == 200) {
                logger.info("Current embold scanner package is at checksum: {}, downloading latest embold scanner package...", checksum);
                File tgzFile = response.getBody();
                logger.debug("Downloaded file: {}, size: {} bytes", destFile, FileUtils.sizeOf(tgzFile));
                return true;
            } else if (response.getStatus() == 204) {
                // Scanner is upto-date as the requested checksum and available package on server matches
                logger.info("Current embold scanner package is at checksum: {}, skipping update", checksum);
                return false;
            }
        }

        // Should not reach here. Some other unexpected code, do not proceed
        logger.error("Error downloading embold scanner packages with error code: {} ", response.getStatus());
        throw new SyncException("Error downloading embold scanner packages with error code: " + response.getStatus());
    }
}
