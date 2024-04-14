package io.embold.scan;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import java.net.HttpURLConnection;

/**
 * This class gets the server product version via its REST APIs
 */
public class EmboldVersion {

    private static final Logger logger = org.apache.logging.log4j.LogManager.getLogger(EmboldVersion.class);
    private final String name;

    EmboldVersion(String name) {
        this.name = name;
    }

    /**
     *
     * @param url Embold Server URL
     * @param token Embold Access Token
     * @return Server version
     * @throws SyncException throws when server returns error
     */
    public static EmboldVersion getVersion(String url, String token) throws SyncException {
        EmboldVersion result = new EmboldVersion("V1");
        try {
            if ((StringUtils.substringAfterLast(url, "/")).equals("")) {
                url = StringUtils.substringBeforeLast(url, "/");
            }
            logger.info("Product details URL: " + url + "/api/product/details");
            HttpResponse<JsonNode> details = Unirest.get(url + "/api/product/details")
                    .header("Authorization", "Bearer " + token)
                    .asJson();
            if (details != null) {
                JsonNode jsonNode = details.getBody();
                logger.info("Product details - " + jsonNode.toPrettyString());
                int statusCode = details.getStatus();
                if (HttpURLConnection.HTTP_OK == statusCode) {
                    String serverType = details.getBody().getObject().getString("productVersion");
                    String userType = details.getBody().getObject().getJSONObject("license").getString("type");
                    logger.info("serverType: " + serverType + ", userType: " + userType);
                    result = new EmboldVersion(serverType);
                } else {
                    logger.error("Server version check returned non-ok status code: " + details.getStatus());
                    throw new SyncException("Server version check returned non-ok status code: " + details.getStatus());
                }
            } else {
                logger.info("Product details response is null, assuming V1");
            }
        } catch (UnirestException e) {
            logger.error("Error occurred while getting server version", e);
            throw new SyncException(e);
        }

        return result;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }
}
