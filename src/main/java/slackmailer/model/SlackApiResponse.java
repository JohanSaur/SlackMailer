package slackmailer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SlackApiResponse {
    private boolean ok;
    private String stuff;
    private String warning;
    private String error;
    private Person person;

    private SlackApiResponse() {}

    public SlackApiResponse(boolean ok, String error, Person person) {
        this.ok     = ok;
        this.error  = error;
        this.person = person;
    }

    public boolean isOk() { return ok; }

    public String getStuff() { return stuff; }

    public String getWarning() { return warning; }

    public String getError() { return error; }

    public Person getPerson() { return person; }

    public void setPerson(Person person) { this.person = person; }
}
