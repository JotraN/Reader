package io.github.jotran.reader.model;

import android.content.Context;

import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.models.Submission;

import java.util.List;
import java.util.Set;

import rx.Observable;

public class DataManager {
    private JrawReaderHelper mJrawHelper;
    private SubmissionDbHelper mDbHelper;

    /**
     * Constructor for a {@code DataManager}.
     *
     * @param context the application context used to access the app's database
     */
    public DataManager(Context context) {
        mJrawHelper = JrawReaderHelper.getInstance();
        mDbHelper = new SubmissionDbHelper(context);
    }

    /**
     * Gets the url used to ask the user for permission.
     *
     * @return the url used to ask the user for permission
     */
    public String getAuthUrl() {
        return mJrawHelper.getAuthUrl();
    }

    /**
     * Determines whether the client is authenticated.
     *
     * @return true if the client is authenticated
     */
    public boolean isAuthenticated() {
        return mJrawHelper.isAuthenticated();
    }

    /**
     * Gets the user name associated with the authenticated client.
     *
     * @return gets the user name associated with the authenticated client
     */
    public String getUserName(){
        return mJrawHelper.getUserName();
    }

    /**
     * Clears any data stored in the application's database.
     */
    public void clear() {
        mDbHelper.reset();
    }

    /**
     * Gets the deferred observable used for authenticating using the given url.
     *
     * @param url the url to authenticate with
     * @return the deferred observable used for authenticating the given url
     */
    public Observable<String> authenticateUrl(String url) {
        return Observable.defer(() -> {
            try {
                return Observable.just(mJrawHelper.authenticateUrl(url));
            } catch (OAuthException e) {
                return Observable.error(e);
            }
        });
    }

    /**
     * Gets the deferred observable used for authenticating using the given
     * refresh token.
     *
     * @param refreshToken the refresh toke to authenticate with
     * @return the deferred observable used for authenticating the given
     * refresh token
     */
    public Observable<Boolean> authenticateToken(String refreshToken) {
        return Observable.defer(() -> {
            try {
                return Observable.just(mJrawHelper.authenticateToken(refreshToken));
            } catch (OAuthException e) {
                return Observable.error(e);
            }
        });
    }

    /**
     * Gets the deferred observable used for logging the user out.
     *
     * @return the deferred observable used for logging the user out
     */
    public Observable<Boolean> logout() {
        return Observable.defer(() -> Observable.just(mJrawHelper.logout()));
    }

    /**
     * Gets the deferred observable used for downloading saved submissions.
     *
     * @return the deferred observable used for downloading saved submissions
     */
    public Observable<List<Submission>> downloadSubmissions() {
        return Observable.defer(() -> Observable.concat(downloadDbSubmissions(),
                downloadNetworkSubmissions()).first(submissions -> !submissions.isEmpty()));
    }

    /**
     * Gets the deferred observable used for downloading saved submissions from the stored database.
     * <p>
     * The page of submissions are added to the database on next.
     *
     * @return the deferred observable used for downloading the first page of
     * saved submissions
     */
    public Observable<List<Submission>> downloadDbSubmissions() {
        return Observable.defer(() -> Observable.just(mDbHelper.getSubmissions()));
    }

    /**
     * Gets the deferred observable used for downloading the first page of
     * saved submissions from the client.
     *
     * @return the deferred observable used for downloading the first page of
     * saved submissions
     */
    public Observable<List<Submission>> downloadNetworkSubmissions() {
        return Observable.defer(() -> Observable.just(mJrawHelper.download())
                .doOnNext(submissions -> mDbHelper.addSubmissions(submissions)));
    }

    /**
     * Gets the deferred observable used for downloading the next page of
     * saved submissions from the client.
     * <p>
     * The page of submissions are added to the database on next.
     *
     * @return the deferred observable used for downloading the next page of
     * saved submissions
     */
    public Observable<List<Submission>> downloadNextSubmissions() {
        return Observable.defer(() -> Observable.just(mJrawHelper.downloadNext())
                .doOnNext(submissions -> mDbHelper.addSubmissions(submissions)));
    }

    /**
     * Gets the deferred observable used for downloading the set of subreddits built from the
     * stored database's saved submissions.
     *
     * @return the deferred observable used for downloading the set of subreddits
     */
    public Observable<Set<String>> downloadSubreddits() {
        return Observable.defer(() -> Observable.just(mDbHelper.getSubreddits()));
    }
}
