package io.github.jotran.reader.view.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import io.github.jotran.reader.R;
import io.github.jotran.reader.model.JrawReaderHelper;
import io.github.jotran.reader.presenter.LoginPresenter;


public class LoginFragment extends Fragment implements LoginPresenter.LoginView {
    private LoginFragmentListener mListener;
    private LoginPresenter mPresenter;
    private WebView mWebView;

    public interface LoginFragmentListener {
        void onLoggedIn();
    }

    public LoginFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = new LoginPresenter(getContext(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);
        mWebView = (WebView) v.findViewById(R.id.webview);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (Uri.parse(url).getHost().equals(Uri
                        .parse(JrawReaderHelper.REDIRECT_URL).getHost())) {
                    mPresenter.authenticate(url);
                    return true;
                }
                return false;
            }
        });
        clearCookies();
        mPresenter.authenticate();
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof LoginFragmentListener) {
            mListener = (LoginFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void clearCookies() {
        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeAllCookies(null);
        } else cookieManager.removeAllCookie();
    }

    @Override
    public void showAuthenticated() {
        if (mListener != null)
            mListener.onLoggedIn();
    }

    @Override
    public void showLogin(String authUrl) {
        if (mWebView != null)
            mWebView.loadUrl(authUrl);
    }

    @Override
    public void showError(Throwable e) {
        Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
    }
}