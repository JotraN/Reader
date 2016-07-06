package io.github.jotran.reader.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import net.dean.jraw.models.Submission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import io.github.jotran.reader.view.activity.MainActivity;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SubmissionsPresenter extends BasePresenter {
    private SubmissionsView mView;

    public interface SubmissionsView {
        void showAuthenticated();

        void showLogin();

        void showSubmissions(List<Submission> submissions);

        void showMoreSubmissions(List<Submission> submissions);

        void showSubreddits(Collection<String> subreddits);

        void showProgressIndicator(boolean show);

        void showError(Throwable e);
    }

    public SubmissionsPresenter(Context context, SubmissionsView view) {
        super(context);
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
                .subscribe(new SubmissionsSubscriber() {
                    @Override
                    public void onNext(List<Submission> submissions) {
                        if (subreddit != null)
                            mView.showSubmissions(filterSubmissions(submissions, subreddit));
                        else {
                            downloadSubreddits(submissions);
                            mView.showSubmissions(submissions);
                        }
                    }
                });
    }

    /**
     * Filters the given list of submissions by the given subreddit.
     *
     * @param submissions the list of submissions to filter
     * @param subreddit   the subreddit to filter by
     * @return the list of filtered submissions
     */
    // TODO Use DataManager to filter.
    private List<Submission> filterSubmissions(List<Submission> submissions, String subreddit) {
        List<Submission> filteredSubmissions = new ArrayList<>();
        for (Submission submission : submissions)
            if (submission.getSubredditName().equals(subreddit))
                filteredSubmissions.add(submission);
        return filteredSubmissions;
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
                .subscribe(new SubmissionsSubscriber() {
                    @Override
                    public void onNext(List<Submission> submissions) {
                        if (subreddit != null)
                            mView.showMoreSubmissions(filterSubmissions(submissions, subreddit));
                        else {
                            downloadSubreddits(submissions);
                            mView.showMoreSubmissions(submissions);
                        }
                    }
                });
    }

    /**
     * Subscriber used to subscribe to a list of submissions.
     */
    private abstract class SubmissionsSubscriber
            extends Subscriber<List<Submission>> {

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

    /**
     * Downloads the unique subreddits belonging to the submissions.
     */
    public void downloadSubreddits(List<Submission> submissions) {
        Set<String> subreddits = new TreeSet<>();
        for (Submission submission : submissions)
            subreddits.add(submission.getSubredditName());
        mView.showSubreddits(subreddits);
    }
}