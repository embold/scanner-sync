package io.embold.scan.exec;

import java.util.Locale;

public final class OsCheck {
    /**
     * types of Operating Systems
     */
    public enum OSType {
        Windows("windows"), MacOS("macos"), Linux("linux"), Other("other");

        private final String osname;

        OSType(String osname) {
            this.osname = osname;
        }

        public String osname() {
            return this.osname;
        }
    };

    // cached result of OS detection
    private static OSType detectedOS;

    /**
     * detect the operating system from the os.name System property and cache
     * the result
     *
     * @return - the operating system detected
     */
    public static OSType getOperatingSystemType() {
        if (detectedOS == null) {
            String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
            if ((OS.contains("mac")) || (OS.contains("darwin"))) {
                detectedOS = OSType.MacOS;
            } else if (OS.contains("win")) {
                detectedOS = OSType.Windows;
            } else if (OS.contains("nux")) {
                detectedOS = OSType.Linux;
            } else {
                detectedOS = OSType.Other;
            }
        }
        return detectedOS;
    }
}
