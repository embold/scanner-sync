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
    protected static OSType detectedOS;

    /**
     * detect the operating system from the os.name System property and cache
     * the result
     *
     * @returns - the operating system detected
     */
    public static OSType getOperatingSystemType() {
        if (detectedOS == null) {
            String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
            if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
                detectedOS = OSType.MacOS;
            } else if (OS.indexOf("win") >= 0) {
                detectedOS = OSType.Windows;
            } else if (OS.indexOf("nux") >= 0) {
                detectedOS = OSType.Linux;
            } else {
                detectedOS = OSType.Other;
            }
        }
        return detectedOS;
    }
}
