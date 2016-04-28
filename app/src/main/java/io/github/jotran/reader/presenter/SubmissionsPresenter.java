package io.github.jotran.reader.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import net.dean.jraw.models.Submission;

import java.util.List;

import io.github.jotran.reader.model.DataManager;
import io.github.jotran.reader.model.SubmissionDbHelper;
import io.github.jotran.reader.view.activity.SubmissionsActivity;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SubmissionsPresenter {
    private final String PREFS_REFRESH_TOKEN = "REFRESH_TOKEN";
    private SubmissionsView mView;
    private DataManager mDataManager;
    private Context mContext;
    private SubmissionDbHelper mDbHelper;

    public interface SubmissionsView {
        void showLogin(String authUrl);

        void showSubmissions(List<Submission> submissions);

        void showMoreSubmissions(List<Submission> submissions);

        void showProgressIndicator(boolean show);

        void showError(Throwable e);
    }

    public SubmissionsPresenter(SubmissionsView view, Context context) {
        mView = view;
        mContext = context;
        mDataManager = new DataManager();
        mDbHelper = new SubmissionDbHelper(context);
        // DB should always be clean/new every time.
        mDbHelper.reset(mDbHelper.getWritableDatabase());
    }

    /**
     * Attempts to authenticate the client.
     * Shows the login screen if the user is logged out.
     */
    public void authenticate() {
        SharedPreferences prefs = mContext
                .getSharedPreferences(SubmissionsActivity.PREFS_NAME, 0);
        String refreshToken = prefs.getString(PREFS_REFRESH_TOKEN, null);
        boolean loggedOut = refreshToken == null;
        if (loggedOut) mView.showLogin(mDataManager.getAuthUrl());
        else authenticateToken(refreshToken);
    }

    /**
     * Authenticates the client using the given url.
     * Saves the refresh token once authentication has succeeded.
     *
     * @param url the url to authenticate with
     */
    public void authenticate(String url) {
        mView.showProgressIndicator(true);
        mDataManager.authenticateUrl(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(refreshToken -> {
                    if (refreshToken != null) {
                        SharedPreferences prefs = mContext
                                .getSharedPreferences(SubmissionsActivity.PREFS_NAME, 0);
                        prefs.edit().putString(PREFS_REFRESH_TOKEN, refreshToken)
                                .apply();
                        downloadSubmissions();
                    } else
                        mView.showError(new Exception("Authentication failed."));
                });
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
                    if (authenticated) downloadSubmissions();
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
                    if (!loggedOut)
                        mView.showError(new Exception("Logging out failed."));
                });
    }

    /**
     * Downloads the first page of saved submissions.
     */
    public void downloadSubmissions() {
        // TODO Load from db instead.
        mView.showProgressIndicator(true);
        mDataManager.downloadSubmissions()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SubmissionsSubscriber() {
                    @Override
                    public void onNext(List<Submission> submissions) {
                        mView.showSubmissions(submissions);
                        SQLiteDatabase db = mDbHelper.getWritableDatabase();
                        mDbHelper.addSubmissions(db, submissions);
                        db.close();
                    }
                });
    }

    /**
     * Downloads the next page of saved submissions.
     */
    public void downloadNextSubmissions() {
        mView.showProgressIndicator(true);
        mDataManager.downloadNextSubmissions()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SubmissionsSubscriber() {
                    @Override
                    public void onNext(List<Submission> submissions) {
                        mView.showMoreSubmissions(submissions);
                        SQLiteDatabase db = mDbHelper.getWritableDatabase();
                        mDbHelper.addSubmissions(db, submissions);
                        db.close();
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
}