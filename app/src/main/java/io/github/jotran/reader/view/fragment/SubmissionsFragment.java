package io.github.jotran.reader.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import net.dean.jraw.models.Submission;

import java.util.List;

import io.github.jotran.reader.R;
import io.github.jotran.reader.presenter.SubmissionsPresenter;
import io.github.jotran.reader.view.activity.LoginActivity;
import io.github.jotran.reader.view.activity.SubmissionsActivity;
import io.github.jotran.reader.view.adapter.SubmissionsRecyclerAdapter;

public class SubmissionsFragment extends Fragment implements
        SubmissionsPresenter.SubmissionsView {
    private RecyclerView mRecyclerView;
    private SubmissionsRecyclerAdapter mAdapter;
    private ProgressBar mProgressBar;
    private SubmissionsPresenter mPresenter;

    public SubmissionsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setHasOptionsMenu(true);
        mPresenter = new SubmissionsPresenter(this, getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);

        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout)
                v.findViewById(R.id.swipeSaved);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            mPresenter.downloadSubmissions();
            swipeRefreshLayout.setRefreshing(false);
        });
        mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(getActivity(),
                        LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mPresenter.authenticate();
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                logout();
                return true;
            case R.id.action_sync:
                mPresenter.downloadSubmissions();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LoginActivity.REQUEST_CODE
                && resultCode == Activity.RESULT_OK && data != null) {
            String url = data.getExtras().getString(LoginActivity.RETURN_URL);
            mPresenter.authenticate(url);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void showLogin(String authUrl) {
        login(authUrl);
    }

    @Override
    public void showSubmissions(List<Submission> submissions) {
        mAdapter = new SubmissionsRecyclerAdapter(submissions);
        mAdapter.setSubmissionsListener(this::loadSubmission);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setVisibility(View.VISIBLE);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager lm = (LinearLayoutManager)
                        recyclerView.getLayoutManager();
                int totalItems = lm.getItemCount();
                int pastVisibleItems = lm.findFirstVisibleItemPosition();
                int visibleItems = recyclerView.getChildCount();
                boolean bottomReached = totalItems <=
                        (visibleItems + pastVisibleItems);
                if (bottomReached && !mProgressBar.isShown())
                    mPresenter.downloadNextSubmissions();
            }
        });
    }

    @Override
    public void showMoreSubmissions(List<Submission> submissions) {
        if (mAdapter != null) mAdapter.addSubmissions(submissions);
    }

    @Override
    public void showSubreddits(List<String> subreddits) {
       // TODO Implement
    }

    @Override
    public void showProgressIndicator(boolean show) {
        if (show) mProgressBar.setVisibility(View.VISIBLE);
        else mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void showError(Throwable e) {
        Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
    }

    private void loadSubmission(Submission submission) {
        Uri uri = Uri.parse("http://reddit.com/" + submission.getPermalink());
        startActivity(new Intent(Intent.ACTION_VIEW).setData(uri));
    }

    private void login(String authUrl) {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.putExtra(LoginActivity.LOGIN_URL, authUrl);
        startActivityForResult(intent, LoginActivity.REQUEST_CODE);
    }

    private void logout() {
        mRecyclerView.setVisibility(View.GONE);
        SharedPreferences prefs = getActivity()
                .getSharedPreferences(SubmissionsActivity.PREFS_NAME, 0);
        prefs.edit().clear().apply();
        mPresenter.logout();
        mPresenter.authenticate();
    }

}
