package io.embold.scan.exec;

import io.embold.scan.SyncException;
import io.embold.scan.SyncOpts;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public class Extractor {
    private static Logger logger = LogManager.getLogger(Extractor.class);

    private static PosixFilePermission[] allPermissions = PosixFilePermission.values();
    private static OsCheck.OSType os = OsCheck.getOperatingSystemType();

    private Extractor(){}

    public static void tgzExtract(File tgzFile, File destDir) throws SyncException {

        logger.info("Extracting package {}...", tgzFile);

        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        String outputFile = getFileName(tgzFile, destDir.getAbsolutePath());
        File tarFile = new File(outputFile);
        try {
            // Calling method to decompress file
            deCompressGZipFile(tgzFile, tarFile);

            // Calling method to untar file
            unTarFile(tarFile, destDir);
        } finally {
            FileUtils.deleteQuietly(tarFile);
        }

        logger.info("Extracted package {} at {}", tgzFile, destDir);
    }

    private static File deCompressGZipFile(File gZippedFile, File tarFile) throws SyncException {
        try (FileInputStream fis = new FileInputStream(gZippedFile)) {
            try (GZIPInputStream gZIPInputStream = new GZIPInputStream(fis)) {
                try (FileOutputStream fos = new FileOutputStream(tarFile)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = gZIPInputStream.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    return tarFile;
                }
            }
        } catch (IOException e) {
            throw new SyncException("Error decompressing embold scanner files", e);
        }
    }

    private static void unTarFile(File tarFile, File destFile) throws SyncException {
        try (FileInputStream fis = new FileInputStream(tarFile)) {
            try (TarArchiveInputStream tis = new TarArchiveInputStream(fis)) {
                TarArchiveEntry tarEntry = null;

                // tarIn is a TarArchiveInputStream
                while ((tarEntry = tis.getNextTarEntry()) != null) {
                    File outputFile = new File(destFile + File.separator + tarEntry.getName());
                    if (tarEntry.isDirectory()) {
                        if (!outputFile.exists()) {
                            outputFile.mkdirs();
                        }
                    } else {
                        outputFile.getParentFile().mkdirs();
                        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                            IOUtils.copy(tis, fos);
                        }

                        if(!os.equals(OsCheck.OSType.Windows)) {
                            int mode = tarEntry.getMode() & 0777;
                            if (mode != 0) {
                                Files.setPosixFilePermissions(fileToPath(outputFile), modeToPermissions(mode));
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new SyncException("Error downloading embold scanner", e);
        }
    }

    private static String getFileName(File inputFile, String outputFolder) {
        return outputFolder + File.separator + inputFile.getName().substring(0, inputFile.getName().lastIndexOf('.'));
    }

    private static Path fileToPath(File file) throws SyncException {
        try {
            return file.toPath();
        } catch (InvalidPathException e) {
            throw new SyncException("Invalid file path", e);
        }
    }

    private static Set<PosixFilePermission> modeToPermissions(int mode) throws IOException {
        // Anything larger is a file type, not a permission.
        int PERMISSIONS_MASK = 07777;
        // setgid/setuid/sticky are not supported.
        int MAX_SUPPORTED_MODE = 0777;
        mode = mode & PERMISSIONS_MASK;
        if ((mode & MAX_SUPPORTED_MODE) != mode) {
            throw new IOException("Invalid mode: " + mode);
        }

        Set<PosixFilePermission> result = EnumSet.noneOf(PosixFilePermission.class);
        for (int i = 0; i < allPermissions.length; i++) {
            if ((mode & 1) == 1) {
                result.add(allPermissions[allPermissions.length - i - 1]);
            }
            mode >>= 1;
        }
        return result;
    }
}
