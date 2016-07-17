package io.github.jotran.reader.view.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.TextView;

import java.util.Collection;

import io.github.jotran.reader.R;
import io.github.jotran.reader.view.fragment.SubmissionsFragment;

public class MainActivity extends AppCompatActivity {
    public static final String PREFS_REFRESH_TOKEN = "REFRESH_TOKEN";
    public static final String PREFS_USER_NAME = "USER_NAME";
    public static final String PREFS_NAME = "READER_PREFS";

    private CollapsingToolbarLayout mCollapsingToolbar;
    /**
     * The group used to identify menu items representing subreddits.
     */
    private final int SUBREDDIT_MENU_GRP = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_NAME, 0);
        String refreshToken = prefs.getString(PREFS_REFRESH_TOKEN, null);
        if (refreshToken == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        }
        String userName = prefs.getString(PREFS_USER_NAME, null);
        if(userName != null){
            NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
            TextView navHeaderText = (TextView) navView.getHeaderView(0).findViewById(R.id.nav_header_text);
            navHeaderText.setText(userName);

        }
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            setSubFragment(null);
        } else {
            // TODO Properly handle this.
            View view = findViewById(R.id.fragment_container);
            Snackbar.make(view, "Please connect to the internet.", Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Sets the appropriate {@code SubmissionsFragment} using the given subreddit.
     * If none is supplied, a {@code SubmissionsFragment} with no subreddit is used.
     *
     * @param subreddit the subreddit to set the {@code SubmissionsFragment} to, use null no
     *                  subreddit is necessary
     */
    private void setSubFragment(String subreddit) {
        if (mCollapsingToolbar != null) {
            if (subreddit == null)
                mCollapsingToolbar.setTitle(getResources().getString(R.string.app_name));
            else mCollapsingToolbar.setTitle(subreddit);
        }
        SubmissionsFragment fragment = new SubmissionsFragment();
        fragment.setSubmissionsFragmentListener(this::setupNavMenu);
        if (subreddit != null) {
            Bundle args = new Bundle();
            args.putString(SubmissionsFragment.SUBREDDIT, subreddit);
            fragment.setArguments(args);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment).commit();
    }

    /**
     * Setups the navigation drawer's menu to include the given collection of subreddits.
     * The subreddit portion of this menu is rebuilt each time.
     *
     * @param subreddits the collection of subreddits to add to the menu
     */
    private void setupNavMenu(Collection<String> subreddits) {
        NavigationView mNavView = (NavigationView) findViewById(R.id.nav_view);
        if (subreddits.isEmpty() || mNavView == null) return;
        Menu menu = mNavView.getMenu();
        menu.removeGroup(SUBREDDIT_MENU_GRP);
        SubMenu subMenu = menu.addSubMenu(SUBREDDIT_MENU_GRP, Menu.NONE, Menu.NONE, "Subreddit");
        for (String subreddit : subreddits) {
            subMenu.add(SUBREDDIT_MENU_GRP, Menu.NONE, Menu.NONE, subreddit);
        }
        subMenu.setGroupCheckable(SUBREDDIT_MENU_GRP, true, true);
        mNavView.setNavigationItemSelectedListener(item -> {
            selectNavItem(item);
            return true;
        });
    }

    /*
     * Calls the appropriate function to based on the given {@code MenuItem}.
     *
     * @param item the {@code MenuItem} that was selected
     */
    private void selectNavItem(MenuItem item) {
        if (item.getItemId() == R.id.nav_all_fragment)
            setSubFragment(null);
        else if (item.getGroupId() == SUBREDDIT_MENU_GRP) {
            setSubFragment(item.getTitle().toString());
        }
        item.setChecked(true);
        DrawerLayout mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawer != null) mDrawer.closeDrawers();
    }
}