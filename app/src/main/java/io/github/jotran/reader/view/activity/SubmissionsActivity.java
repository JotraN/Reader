package io.github.jotran.reader.view.activity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import io.github.jotran.reader.R;
import io.github.jotran.reader.view.fragment.SubmissionsFragment;

public class SubmissionsActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "READER_PREFS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ConnectivityManager cm =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SubmissionsFragment()).commit();
        } else {
            // TODO Properly handle this.
            View view = findViewById(R.id.fragment_container);
            Snackbar.make(view, "Please connect to the internet.", Snackbar.LENGTH_SHORT).show();
        }
    }
}