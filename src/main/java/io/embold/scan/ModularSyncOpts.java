package io.embold.scan;

import io.embold.scan.exec.Constants;

import java.io.File;
import java.util.Set;

public class ModularSyncOpts extends SyncOpts {

    /**
     * Set of packages to be synched
     */
    private final Set<Package> packages;

    /**
     * Location where package files should be downloaded
     */
    private final String coronaPackageCache;

    public ModularSyncOpts(String emboldUrl, String emboldToken, String coronaLocation, Set<Package> packages) throws SyncException {
        super(emboldUrl, emboldToken, coronaLocation);
        this.packages = packages;
        this.coronaPackageCache = coronaLocation + File.separator + Constants.CORONA_PKG_CACHE;
        File f = new File(this.coronaPackageCache);
        if (!f.exists()) {
            if (!f.mkdirs()) {
                throw new SyncException("Failed to create package cache dir: " + f);
            }
        }
    }

    public Set<Package> getPackages() {
        return packages;
    }

    public String getCoronaPackageCache() {
        return coronaPackageCache;
    }
}
