package io.github.jotran.reader.view.fragment;

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

import java.util.ArrayList;
import java.util.Collection;

import io.github.jotran.reader.R;
import io.github.jotran.reader.presenter.SubmissionsPresenter;
import io.github.jotran.reader.view.activity.LoginActivity;
import io.github.jotran.reader.view.activity.MainActivity;
import io.github.jotran.reader.view.adapter.SubmissionsRecyclerAdapter;

public class SubmissionsFragment extends Fragment implements
        SubmissionsPresenter.SubmissionsView {
    public static final String SUBREDDIT = "SUBREDDIT";
    private RecyclerView mRecyclerView;
    private SubmissionsRecyclerAdapter mAdapter;
    private ProgressBar mProgressBar;
    private SubmissionsPresenter mPresenter;
    private SubmissionsFragmentListener mListener;
    private String mSubreddit;

    public interface SubmissionsFragmentListener {
        void onSubmissionsLoaded(Collection<String> subreddits);
    }

    public SubmissionsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setHasOptionsMenu(true);
        mPresenter = new SubmissionsPresenter(getContext(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getArguments() != null && getArguments().containsKey(SUBREDDIT))
            mSubreddit = getArguments().getString(SUBREDDIT);

        View v = inflater.inflate(R.layout.fragment_main, container, false);

        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout)
                v.findViewById(R.id.swipeSaved);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshSubmissions();
            swipeRefreshLayout.setRefreshing(false);
        });

        mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new SubmissionsRecyclerAdapter(new ArrayList<>());
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
                    mPresenter.downloadNextSubmissions(mSubreddit);
            }
        });

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
            case R.id.action_refresh:
                refreshSubmissions();
                return true;
            case R.id.action_dl_next:
                mPresenter.downloadNextSubmissions(mSubreddit);
                return true;
            case R.id.action_logout:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showAuthenticated() {
        mPresenter.downloadSubmissions(mSubreddit);
    }

    @Override
    public void showLogin() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void showSubmission(Submission submission) {
        if (mAdapter != null)
            mAdapter.addSubmissions(submission);
    }

    @Override
    public void showSubreddits(Collection<String> subreddits) {
        if (mListener != null)
            mListener.onSubmissionsLoaded(subreddits);
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

    private void refreshSubmissions() {
        mAdapter.clear();
        mPresenter.refreshSubmissions(mSubreddit);
    }

    private void loadSubmission(Submission submission) {
        Uri uri = Uri.parse(submission.getShortURL());
        startActivity(new Intent(Intent.ACTION_VIEW).setData(uri));
    }

    private void logout() {
        mRecyclerView.setVisibility(View.GONE);
        SharedPreferences prefs = getActivity()
                .getSharedPreferences(MainActivity.PREFS_NAME, 0);
        prefs.edit().clear().apply();
        mPresenter.logout();
    }

    public void setSubmissionsFragmentListener(SubmissionsFragmentListener listener) {
        mListener = listener;
    }
}
