package slackmailer.config;

import slackmailer.model.Person;

import java.util.List;

public class Config {
    private SlackSettings slackSettings;
    private List<Person> persons;

    public SlackSettings getSlackSettings() { return slackSettings; }

    public List<Person> getPersons() { return persons; }
}
