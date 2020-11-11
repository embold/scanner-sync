package io.embold.scan;

import io.embold.scan.exec.Constants;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

public class SyncOpts {
    /**
     * URL of the Embold Server
     */
    private String emboldUrl;
    /**
     * Embold Access Token
     */
    private String emboldToken;
    /**
     * Location where corona should be copied after sync (this is the parent directory of the "corona" directory)
     */
    private String coronaLocation;

    /**
     * This is the result of <coronaLocation> + "/corona"
     */
    private String coronaHome;


    public SyncOpts(String emboldUrl, String emboldToken, String coronaLocation) {
        this.emboldUrl = emboldUrl;
        if(StringUtils.endsWith(emboldUrl, "/")) {
            this.emboldUrl = StringUtils.substringBeforeLast(emboldUrl, "/");
        }

        this.emboldToken = emboldToken;
        this.coronaLocation = coronaLocation;
        this.coronaHome = coronaLocation + File.separator + Constants.CORONA;
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
}
