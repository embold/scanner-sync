package io.embold.scan;

import io.embold.scan.exec.Constants;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

public class SyncOpts {
    /**
     * URL of the Embold Server
     */
    private final String emboldUrl;
    /**
     * Embold Access Token
     */
    private final String emboldToken;
    /**
     * Location where corona should be copied after sync (this is the parent directory of the "corona" directory)
     */
    private final String coronaLocation;

    /**
     * This is the result of <coronaLocation> + "/corona"
     */
    private final String coronaHome;

    /**
     * This fetches and stores the server version (used for subsequent API calls)
     */
    private final EmboldVersion emboldVersion;

    public SyncOpts(String emboldUrl, String emboldToken, String coronaLocation) throws SyncException {
        this.emboldUrl = emboldUrl;
//        if(StringUtils.endsWith(emboldUrl, "/")) {
//            this.emboldUrl = StringUtils.substringBeforeLast(emboldUrl, "/");
//        }

        this.emboldToken = emboldToken;
        this.coronaLocation = coronaLocation;
        this.coronaHome = coronaLocation + File.separator + Constants.CORONA;
        if(StringUtils.isNotEmpty(this.emboldUrl) && StringUtils.isNotEmpty(this.emboldToken)) {
            this.emboldVersion = EmboldVersion.getVersion(this.emboldUrl, this.emboldToken);
        } else {
            this.emboldVersion = null;
        }
    }

    public String getEmboldUrl() {
        return emboldUrl;
    }

    public String getEmboldToken() {
        return emboldToken;
    }

    public String getCoronaLocation() {
        return coronaLocation;
    }

    public String getCoronaHome() {
        return coronaHome;
    }

    public EmboldVersion getEmboldVersion() {
        return emboldVersion;
    }
}
