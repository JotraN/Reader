package io.github.jotran.reader.util;

import net.dean.jraw.models.Submission;
import net.dean.jraw.util.JrawUtils;

public class MockSubmission {

    private Submission submission;

    public MockSubmission(String title, String author, String date, String subreddit) {

        submission = new Submission(JrawUtils.fromString("" +
                "{  \n" +
                "   \"subreddit\":\"" + subreddit + "\",\n" +
                "   \"author\":\"" + author + "\",\n" +
                "   \"title\":\"" + title + "\",\n" +
                "   \"created_utc\":" + date + "\n" +
                "}"));
    }

    public Submission getSubmission() {
        return submission;
    }
}
