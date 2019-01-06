package slackmailer;

public class SlackInviteClientTest {
    /*
     * This class should setup a local http server as a mock, that can reply with different SlackApiReplys depending
     * on the data that comes in. That way we can test that SlackInviteClient.invite(person) works properly
     * After that the same local http server can be used to test SlackMailer itself with a list of people to see that
     * the correct amount of invites is reported.
     */
}
