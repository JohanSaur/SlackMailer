package slackmailer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slackmailer.config.SlackSettings;
import slackmailer.model.Person;
import slackmailer.model.SlackApiResponse;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * The SlackInviteClient is able to contact the Slack API to send invites to a restricted channel.
 * That should be sufficient for the amount of expected people to exist in the list at the same time,
 * but if list tends to grow very large, it might be a good idea to check if Slack API supports bulk postings.
 */
public class SlackInviteClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(SlackInviteClient.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private URL baseUrl;
    private String token;

    public SlackInviteClient(SlackSettings slackSettings) throws MalformedURLException {
        baseUrl = slackSettings.getBaseUri().toURL();
        token   = slackSettings.getToken();
    }

    public SlackApiResponse invite(Person person) {
        /* Learned the hard way that the users.admin.invite request doesn't support posting JSON bodies like the rest
         * of the API, so need to send it as old fashioned x-www-form-urlencoded
         */
        String postDataString = "email=" + person.getEmail() +
               "&first_name=" + person.getFirstName() +
               "&last_name=" + person.getLastName() +
               "&channels=" + person.getChannels() +
                //Following is available for paid teams only, will generate error for "free" slack workspace.
               "&restricted=true"; //Maybe this should be set to ultra_restricted=true to allow one (1) channel only per e-mail.
        byte[] postData = postDataString.getBytes(Charset.forName("UTF-8"));

        try {
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) baseUrl.openConnection();

            httpsURLConnection.setConnectTimeout(5000);
            httpsURLConnection.setReadTimeout(5000);
            //Token can be send as form data or as a header, as long as it's *generated* for scope 'client'.
            //If setting as a header, scope must be set to 'Bearer' in the header.
            //Setting header to Authorization: client xxxx-xxxxxxx will generate not_authed error message
            httpsURLConnection.setRequestProperty("Authorization", "Bearer " + token);
            httpsURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpsURLConnection.setDoOutput(true);
            httpsURLConnection.setDoInput(true);
            httpsURLConnection.setRequestMethod("POST");

            LOGGER.debug("About to post: " + new String(postData));

            OutputStream os = httpsURLConnection.getOutputStream();
            os.write(postData);
            os.flush();
            os.close();

            LOGGER.debug("Server respond status [{}]: {}",
                    httpsURLConnection.getResponseCode(), httpsURLConnection.getResponseMessage());

            InputStream is = httpsURLConnection.getInputStream();

            //Extract the reply from Slack API, Jackson will then close the InputStream and JVM will then shut down
            //the connection
            return extractSlackApiReply(is, person); //httpsURLConnection.getInputStream(), person);
        } catch (SocketTimeoutException e) {
            LOGGER.error("Timeout when contacting Slack API", e);
            return new SlackApiResponse(false, "Timeout when contacting Slack API", person);
        } catch (IOException e) {
            LOGGER.error("Unable to communicate with Slack API", e);
            return new SlackApiResponse(false, "Unable to communicate with Slack API", person);
        }
    }

    private SlackApiResponse extractSlackApiReply(InputStream is, Person person) {
        try {
            SlackApiResponse response = MAPPER.readValue(is, SlackApiResponse.class);
            response.setPerson(person);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Server respond body: {}", MAPPER.writeValueAsString(response));
            }
            return response;
        } catch (IOException e) {
            LOGGER.error("Unable to deserialize reply from Slack API", e);
            return new SlackApiResponse(false, "Unable to deserialize reply from Slack API", person);
        }
    }
}
