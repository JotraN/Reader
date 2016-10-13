package io.github.jotran.reader.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import net.dean.jraw.models.Submission;

import java.util.Collection;

import io.github.jotran.reader.model.DataManager;
import io.github.jotran.reader.view.activity.MainActivity;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SubmissionsPresenter extends BasePresenter {
    private SubmissionsView mView;

    public interface SubmissionsView {
        void showAuthenticated();

        void showLogin();

        void showSubmission(Submission submission);

        void showSubreddits(Collection<String> subreddits);

        void showProgressIndicator(boolean show);

        void showError(Throwable e);
    }

    public SubmissionsPresenter(Context context, SubmissionsView view) {
        super(context);
        mView = view;
    }

    public SubmissionsPresenter(Context context, SubmissionsView view, DataManager dm) {
        super(context, dm);
        mView = view;
    }

    /**
     * Attempts to authenticate the client.
     * Shows the login screen if the user is logged out.
     */
    @Override
    public void authenticate() {
        if (mDataManager.isAuthenticated()) {
            mView.showAuthenticated();
            return;
        }
        // Always clear data when first authenticating.
        mDataManager.clear();
        SharedPreferences prefs = mContext
                .getSharedPreferences(MainActivity.PREFS_NAME, 0);
        String refreshToken = prefs.getString(MainActivity.PREFS_REFRESH_TOKEN, null);
        boolean loggedOut = refreshToken == null;
        if (loggedOut) mView.showLogin();
        else authenticateToken(refreshToken);
    }

    /**
     * Authenticates the client using the given refresh token.
     *
     * @param refreshToken the refresh token to use
     */
    private void authenticateToken(String refreshToken) {
        mView.showProgressIndicator(true);
        mDataManager.authenticateToken(refreshToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(authenticated -> {
                    mView.showProgressIndicator(false);
                    if (authenticated) mView.showAuthenticated();
                    else mView.showError(new Exception("Authentication failed."));
                });
    }

    /**
     * Logs the user out from the client.
     */
    public void logout() {
        mView.showProgressIndicator(true);
        mDataManager.logout()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(loggedOut -> {
                    mView.showProgressIndicator(false);
                    if (loggedOut)
                        mView.showLogin();
                    else
                        mView.showError(new Exception("Logging out failed."));
                });
    }

    /**
     * Refreshes the current list of saved submissions.
     *
     * @param subreddit the subreddit used to filter the list of saved submissions
     */
    public void refreshSubmissions(String subreddit) {
        mDataManager.clear();
        downloadSubmissions(subreddit);
    }

    /**
     * Downloads the first page of saved submissions, filtering the submissions by the given
     * subreddit.
     *
     * @param subreddit the subreddit to filter the submissions by, use null to not filter
     *                  submissions
     */
    public void downloadSubmissions(String subreddit) {
        mView.showProgressIndicator(true);
        mDataManager.downloadSubmissions()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(submissions -> {
                    if (subreddit == null) downloadSubreddits();
                })
                .flatMap(Observable::from)
                .filter(submission -> subredditMatched(subreddit, submission))
                .subscribe(new SubmissionSubscriber());
    }

    /**
     * Downloads the next page of saved submissions, filtering the submissions by the given
     * subreddit.
     *
     * @param subreddit the subreddit to filter by, use null to not filter submissions
     */
    public void downloadNextSubmissions(String subreddit) {
        mView.showProgressIndicator(true);
        mDataManager.downloadNextSubmissions()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(submissions -> {
                    if (subreddit == null) downloadSubreddits();
                })
                .flatMap(Observable::from)
                .filter(submission -> subredditMatched(subreddit, submission))
                .subscribe(new SubmissionSubscriber());
    }

    /**
     * Determines if the given submission belongs to the given subreddit.
     *
     * @param subreddit  the targeted subreddit, a null subreddit always results in a match
     * @param submission the submission to check
     * @return true if the submission belongs to the subreddit
     */
    private boolean subredditMatched(String subreddit, Submission submission) {
        return subreddit == null || submission.getSubredditName().equals(subreddit);
    }

    /**
     * Downloads the set of subreddits belonging to the current list of submissions.
     */
    private void downloadSubreddits() {
        mDataManager.downloadSubreddits()
                .subscribe(subreddits -> mView.showSubreddits(subreddits));
    }

    /**
     * Searches the list of downloaded submissions for submissions that matches the given
     * subreddit and submission title.
     *
     * @param subreddit the targeted subreddit, a null subreddit always results in a match
     * @param title     the submission title to match
     */
    public void searchSubmissions(String subreddit, @NonNull String title) {
        mView.showProgressIndicator(true);
        mDataManager.downloadSubmissions()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(Observable::from)
                .filter(submission -> submission.getTitle().toLowerCase()
                        .contains(title.toLowerCase())
                        && (subredditMatched(subreddit, submission)))
                .subscribe(new SubmissionSubscriber());
    }

    /**
     * Subscriber used to subscribe to a list of submissions.
     */
    private class SubmissionSubscriber extends Subscriber<Submission> {

        @Override
        public void onNext(Submission submission) {
            mView.showSubmission(submission);
        }

        @Override
        public void onCompleted() {
            mView.showProgressIndicator(false);
        }

        @Override
        public void onError(Throwable e) {
            mView.showProgressIndicator(false);
            mView.showError(new Exception("Downloading submissions failed."));
            Log.e("Presenter Error", "Submission Download", e);
        }
    }
}