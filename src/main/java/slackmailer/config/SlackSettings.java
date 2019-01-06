package slackmailer.config;

import com.fasterxml.jackson.annotation.JsonSetter;

import java.net.URI;
import java.net.URISyntaxException;

public class SlackSettings {
    private URI baseUri;
    private String token; //Consider storing the token somewhere safer than in a resource file in the code repository...

    public URI getBaseUri() { return baseUri; }

    @JsonSetter("baseUri")
    public void setBaseUri(URI baseUri) throws URISyntaxException {
        String scheme = "https".equals(baseUri.getScheme().toLowerCase()) ? "https" : "http";
        int port = baseUri.getPort();

        if (port < 0) {
            if ("https".equals(scheme))
                port = 443;
            else
                port = 80;
        }

        this.baseUri = new URI(scheme, null, baseUri.getHost(), port, baseUri.getPath(), null, null);
    }

    public String getToken() { return token; }
}
