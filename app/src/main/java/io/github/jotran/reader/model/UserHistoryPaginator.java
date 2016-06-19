package io.github.jotran.reader.model;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.GenericPaginator;

public class UserHistoryPaginator extends GenericPaginator {
    private String username;

    public UserHistoryPaginator(RedditClient creator, String where,
                                String username) {
        super(creator, Submission.class, where);
        this.username = username;
        setLimit(RECOMMENDED_MAX_LIMIT);
    }

    @Override
    protected String getUriPrefix() {
        return "/user/" + username;
    }

    @Override
    public String[] getWhereValues() {
        return new String[]{"saved"};
    }
}