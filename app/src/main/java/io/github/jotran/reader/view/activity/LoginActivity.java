package io.github.jotran.reader.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import io.github.jotran.reader.R;
import io.github.jotran.reader.view.fragment.LoginFragment;
import io.github.jotran.reader.view.fragment.SplashFragment;

public class LoginActivity extends AppCompatActivity
        implements SplashFragment.SplashFragmentListener, LoginFragment.LoginFragmentListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_login);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.login_fragment_container, new SplashFragment()).commit();
    }

    @Override
    public void onLoginBtnPressed() {
        Bundle args = new Bundle();
        LoginFragment loginFragment = new LoginFragment();
        loginFragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.login_fragment_container, loginFragment).addToBackStack(null).commit();
    }

    @Override
    public void onLoggedIn() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
