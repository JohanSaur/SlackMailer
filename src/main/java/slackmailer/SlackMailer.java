package slackmailer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slackmailer.config.Config;
import slackmailer.model.SlackApiResponse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SlackMailer will read a list of persons in data.json and then send these persons over to the Slack API
 * for Slack to send out an invite to them via e-mail.
 * Finally there is a printout of successful and unsuccessful invites.
 * For an explanation of the error codes from the Slack API, browse to:
 *   https://github.com/ErikKalkoken/slackApiDoc/blob/master/users.admin.invite.md
 */
public class SlackMailer {
    private final static Logger LOGGER = LoggerFactory.getLogger(SlackMailer.class);

    private SlackMailer(Config config) throws MalformedURLException {
        SlackInviteClient slackInviteClient = new SlackInviteClient(config.getSlackSettings());

        List<SlackApiResponse> inviteResults = config.getPersons().stream()
                .map(slackInviteClient::invite)
                .collect(Collectors.toList());

        List<SlackApiResponse> successfulInvites = inviteResults.stream()
                .filter(SlackApiResponse::isOk)
                .collect(Collectors.toList());

        LOGGER.info("Successful invites");
        LOGGER.info("==================");
        successfulInvites.forEach(response -> {
            if (response.getWarning() == null)
                LOGGER.info("Successfully invited {} ({}, {})",
                        response.getPerson().getEmail(), response.getPerson().getLastName(), response.getPerson().getFirstName());
            else
                LOGGER.info("Successfully invited {} ({}, {}), but with a warning: {}",
                        response.getPerson().getEmail(), response.getPerson().getLastName(), response.getPerson().getFirstName(), response.getWarning());
        });

        LOGGER.info("Failed invites");
        LOGGER.info("==============");
        inviteResults.stream().filter(response -> !response.isOk()).forEach(errorResponse ->
                LOGGER.info("Invite not send to {}, due to: {}", errorResponse.getPerson().getEmail(), errorResponse.getError()));

        LOGGER.info("==============");
        LOGGER.info("Finished! There were {} successful invite(s) send from the list of {} person(s)",
                successfulInvites.size(), config.getPersons().size());
    }

    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
        Config config;
        try {
            config = mapper.readValue(SlackMailer.class.getResource("/data.json"), Config.class);
        } catch (IOException e) {
            LOGGER.error("Unable to read settings. Check the 'data.json' file in the resources folder", e);
            System.exit(1);
            return;
        }

        try {
            new SlackMailer(config);
        } catch (MalformedURLException e) {
            LOGGER.error("Slack API URI seems to be malformed. Check the 'data.json' file in the resources folder", e);
            System.exit(1);
        }
    }
}
