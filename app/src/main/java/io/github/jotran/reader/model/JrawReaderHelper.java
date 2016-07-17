package io.github.jotran.reader.model;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.http.oauth.OAuthHelper;
import net.dean.jraw.models.LoggedInAccount;
import net.dean.jraw.models.Submission;

import java.util.ArrayList;
import java.util.List;

public class JrawReaderHelper {
    private static JrawReaderHelper mInstance = new JrawReaderHelper();
    public static final String REDIRECT_URL = "http://127.0.0.1";
    private Credentials mCredentials;
    private OAuthHelper mOAuthHelper;
    private RedditClient mRedditClient;
    private UserHistoryPaginator mPaginator;

    /**
     * Gets the current instance of the {@code JrawReaderHelper}, since {@code JrawReaderHelper} is
     * a singleton.
     *
     * @return the current instance of the {@code JrawReaderHelper}
     */
    public static JrawReaderHelper getInstance() {
        return mInstance;
    }

    private JrawReaderHelper() {
        UserAgent myUserAgent = UserAgent.of("mobile", "io.github.jotran.reader",
                "v0.1", "reader-app");
        mRedditClient = new RedditClient(myUserAgent);
        mOAuthHelper = mRedditClient.getOAuthHelper();
        final String CLIENT_ID = "GvoFjvuX7sAH2g";
        mCredentials = Credentials.installedApp(CLIENT_ID, REDIRECT_URL);
    }

    /**
     * Gets the url used to ask the user for permission.
     *
     * @return the url used to ask the user for permission
     */
    public String getAuthUrl() {
        return mOAuthHelper.getAuthorizationUrl(mCredentials, true, true,
                "identity history").toString();
    }

    /**
     * Determines whether the client is authenticated.
     *
     * @return true if the client is authenticated
     */
    public boolean isAuthenticated() {
        return mRedditClient.isAuthenticated();
    }

    /**
     * Authenticates the client using the given url.
     *
     * @param url the final url retrieved after the gave permission
     * @return the refresh token retrieved upon authentication
     * @throws OAuthException if there was a problem authenticating
     */
    public String authenticateUrl(String url) throws OAuthException {
        OAuthData oAuthData = mOAuthHelper.onUserChallenge(url, mCredentials);
        mRedditClient.authenticate(oAuthData);
        return oAuthData.getRefreshToken();
    }

    /**
     * Authenticates the client using the given refresh token.
     *
     * @param refreshToken the refresh token to use
     * @return true if the authentication was successful
     * @throws OAuthException if there was a problem authenticating
     */
    public boolean authenticateToken(String refreshToken) throws OAuthException {
        mOAuthHelper.setRefreshToken(refreshToken);
        OAuthData oAuthData = mOAuthHelper.refreshToken(mCredentials);
        mRedditClient.authenticate(oAuthData);
        return true;
    }

    /**
     * Logout the client, revoking the tokens used by the client.
     *
     * @return true if the client is no longer authenticated
     * @throws NetworkException if there was a problem logging out
     */
    public boolean logout() throws NetworkException {
        mOAuthHelper.revokeAccessToken(mCredentials);
        mOAuthHelper.revokeRefreshToken(mCredentials);
        return !mRedditClient.isAuthenticated();
    }

    /**
     * Downloads the initial page of saved submissions.
     *
     * @return the list containing the saved submissions
     */
    public List<Submission> download() {
        List<Submission> submissions = new ArrayList<>();
        if (mRedditClient.isAuthenticated()) {
            LoggedInAccount account = mRedditClient.me();
            UserHistoryPaginator historyPaginator =
                    new UserHistoryPaginator(mRedditClient, "saved",
                            account.getFullName());
            if (historyPaginator.hasNext()) {
                for (Object submission : historyPaginator.next()) {
                    submissions.add((Submission) submission);
                }
            }
            mPaginator = historyPaginator;
        }
        return submissions;
    }

    /**
     * Downloads the next page of saved submissions.
     *
     * @return the list containing the next page of saved submissions
     */
    public List<Submission> downloadNext() {
        List<Submission> submissions = new ArrayList<>();
        if (mRedditClient.isAuthenticated() && mPaginator != null) {
            if (mPaginator.hasNext()) {
                for (Object submission : mPaginator.next()) {
                    submissions.add((Submission) submission);
                }
            }
        }
        return submissions;
    }

    /**
     * Gets the user name associated with the authenticated user.
     *
     * @return the user name associated with the authenticated user
     */
    public String getUserName(){
        return mRedditClient.getAuthenticatedUser();
    }
}