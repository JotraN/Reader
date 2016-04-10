package io.github.jotran.reader.model;

import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.models.Submission;

import java.util.List;

import rx.Observable;

public class DataManager {
    private JrawReaderHelper mJrawHelper;

    public DataManager() {
        mJrawHelper = new JrawReaderHelper();
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
     * Gets the deferred observable used for downloading the first page of
     * saved submissions.
     *
     * @return the deferred observable used for downloading the first page of
     * saved submissions
     */
    public Observable<List<Submission>> downloadSubmissions() {
        return Observable.defer(() -> Observable.just(mJrawHelper.download()));
    }

    /**
     * Gets the deferred observable used for downloading the next page of
     * saved submissions.
     *
     * @return the deferred observable used for downloading the next page of
     * saved submissions
     */
    public Observable<List<Submission>> downloadNextSubmissions() {
        return Observable.defer(() ->
                Observable.just(mJrawHelper.downloadNext()));
    }
}
