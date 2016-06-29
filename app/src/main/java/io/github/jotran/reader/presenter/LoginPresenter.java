package io.github.jotran.reader.presenter;

import android.content.Context;
import android.content.SharedPreferences;

import io.github.jotran.reader.view.activity.MainActivity;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LoginPresenter extends BasePresenter {
    private LoginView mView;

    public interface LoginView {
        void showAuthenticated();

        void showLogin(String authUrl);

        void showError(Throwable e);
    }

    public LoginPresenter(Context context, LoginView loginView) {
        super(context);
        mView = loginView;
    }

    /**
     * Shows the login screen for the user to authenticate the client.
     */
    @Override
    public void authenticate() {
        mView.showLogin(mDataManager.getAuthUrl());
    }

    /**
     * Authenticates the client using the given url.
     * Saves the refresh token once authentication has succeeded.
     *
     * @param url the url to authenticate with
     */
    public void authenticate(String url) {
        mDataManager.authenticateUrl(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(refreshToken -> {
                    if (refreshToken != null) {
                        SharedPreferences prefs = mContext
                                .getSharedPreferences(MainActivity.PREFS_NAME, 0);
                        prefs.edit().putString(MainActivity.PREFS_REFRESH_TOKEN, refreshToken)
                                .apply();
                        mView.showAuthenticated();
                    } else
                        mView.showError(new Exception("User authentication failed."));
                });
    }
}
