package io.github.jotran.reader.presenter;

import android.content.Context;

import com.google.common.collect.Lists;

import net.dean.jraw.models.Submission;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.TreeSet;

import io.github.jotran.reader.model.DataManager;
import io.github.jotran.reader.util.MockSubmission;
import io.github.jotran.reader.util.RxSchedulersOverrideRule;
import rx.Observable;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SubmissionsPresenterTest {
    @Rule
    public final RxSchedulersOverrideRule overrideSchedulersRule = new RxSchedulersOverrideRule();

    @Mock
    private SubmissionsPresenter.SubmissionsView submissionsView;

    @Mock
    private DataManager dataManager;

    private List<String> subreddits = Lists.newArrayList("subreddit", "subreddit2");
    private List<MockSubmission> mockSubmissions = Lists.newArrayList(
            new MockSubmission("title", "author", "1", "subreddit"),
            new MockSubmission("title2", "author2", "2", "subreddit2")
    );
    private SubmissionsPresenter presenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(dataManager.downloadSubmissions()).thenReturn(Observable.just(Lists.newArrayList(mockSubmissions.get(0).getSubmission())));
        when(dataManager.downloadSubreddits()).thenReturn(Observable.just(new TreeSet<>(subreddits)));
        when(dataManager.downloadNextSubmissions()).thenReturn(Observable.just(Lists.newArrayList(mockSubmissions.get(1).getSubmission())));
        presenter = new SubmissionsPresenter(Mockito.mock(Context.class), submissionsView, dataManager);
    }

    private void verifyProgressAndNoError() {
        verify(submissionsView, times(1)).showProgressIndicator(true);
        verify(submissionsView, times(1)).showProgressIndicator(false);
        verify(submissionsView, times(0)).showError(any(Throwable.class));
    }


    @Test
    public void downloadSubmissions() {
        presenter.downloadSubmissions(null);
        verify(dataManager, times(1)).downloadSubmissions();
        verify(dataManager, times(1)).downloadSubreddits();
        verify(submissionsView, times(1)).showSubmission(mockSubmissions.get(0).getSubmission());
        verifyProgressAndNoError();
    }

    @Test
    public void downloadSubredditSubmission() {
        presenter.downloadSubmissions(subreddits.get(1));
        verify(dataManager, times(1)).downloadSubmissions();
        verify(submissionsView, times(0)).showSubmission(any(Submission.class));
        verifyProgressAndNoError();
    }

    @Test
    public void downloadMissingSubredditSubmission() {
        presenter.downloadSubmissions("missing_subreddit");
        verify(dataManager, times(1)).downloadSubmissions();
        verify(submissionsView, times(0)).showSubmission(any(Submission.class));
        verifyProgressAndNoError();
    }

    @Test
    public void downloadNextSubmissions() {
        presenter.downloadNextSubmissions(null);
        verify(dataManager, times(1)).downloadNextSubmissions();
        verify(dataManager, times(1)).downloadSubreddits();
        verify(submissionsView, times(1)).showSubmission(mockSubmissions.get(1).getSubmission());
        verifyProgressAndNoError();
    }

    @Test
    public void downloadSubredditNextSubmission() {
        presenter.downloadNextSubmissions(subreddits.get(1));
        verify(dataManager, times(1)).downloadNextSubmissions();
        verify(submissionsView, times(1)).showSubmission(mockSubmissions.get(1).getSubmission());
        verifyProgressAndNoError();
    }

    @Test
    public void downloadMissingSubredditNextSubmission() {
        presenter.downloadNextSubmissions("missing_subreddit");
        verify(dataManager, times(1)).downloadNextSubmissions();
        verify(submissionsView, times(0)).showSubmission(any(Submission.class));
        verifyProgressAndNoError();
    }

    @Test
    public void downloadSubreddits() {
        presenter.downloadSubmissions(null);
        verify(dataManager, times(1)).downloadSubmissions();
        verify(dataManager, times(1)).downloadSubreddits();
        verify(submissionsView, times(1)).showSubreddits(new TreeSet<>(subreddits));
        verifyProgressAndNoError();
    }

    @Test
    public void searchSubmissions() {
        presenter.searchSubmissions(null, "title");
        verify(dataManager, times(1)).downloadSubmissions();
        verify(dataManager, times(0)).downloadSubreddits();
        verify(submissionsView, times(1)).showSubmission(mockSubmissions.get(0).getSubmission());
        verifyProgressAndNoError();
    }

    @Test
    public void searchMissingSubmissions() {
        presenter.searchSubmissions(null, "missing title");
        verify(dataManager, times(1)).downloadSubmissions();
        verify(dataManager, times(0)).downloadSubreddits();
        verify(submissionsView, times(0)).showSubmission(any(Submission.class));
        verifyProgressAndNoError();
    }

    @Test
    public void searchSubmissionsWithUppercaseString() {
        presenter.searchSubmissions(null, "TITLE");
        verify(dataManager, times(1)).downloadSubmissions();
        verify(dataManager, times(0)).downloadSubreddits();
        verify(submissionsView, times(1)).showSubmission(mockSubmissions.get(0).getSubmission());
        verifyProgressAndNoError();
    }

    @Test
    public void searchSubmissionsWithLowercaseString() {
        presenter.searchSubmissions(null, "TITLE");
        verify(dataManager, times(1)).downloadSubmissions();
        verify(dataManager, times(0)).downloadSubreddits();
        verify(submissionsView, times(1)).showSubmission(mockSubmissions.get(0).getSubmission());
        verifyProgressAndNoError();
    }

    @Test
    public void loginWhenNoRefreshToken() {
        when(dataManager.isAuthenticated()).thenReturn(false);
        presenter.authenticate();
        verify(dataManager, times(1)).clear();
        verify(dataManager, times(1)).isAuthenticated();
        verify(dataManager, times(0)).authenticateToken(anyObject());
        verify(dataManager, times(0)).authenticateUrl(anyObject());
        verify(submissionsView, times(0)).showAuthenticated();
        verify(submissionsView, times(1)).showLogin();
    }

    @Test
    public void skipAuthenticationWhenAlreadyAuthenticated() {
        when(dataManager.isAuthenticated()).thenReturn(true);
        presenter.authenticate();
        verify(dataManager, times(0)).clear();
        verify(dataManager, times(1)).isAuthenticated();
        verify(dataManager, times(0)).authenticateToken(anyObject());
        verify(dataManager, times(0)).authenticateUrl(anyObject());
        verify(submissionsView, times(1)).showAuthenticated();
        verify(submissionsView, times(0)).showLogin();
    }

    @Test
    public void logout() {
        when(dataManager.logout()).thenReturn(Observable.just(true));
        presenter.logout();
        verify(submissionsView, times(0)).showError(anyObject());
        verify(submissionsView, times(1)).showLogin();
    }

    @Test
    public void errorWhenLogoutFailed() {
        when(dataManager.logout()).thenReturn(Observable.just(false));
        presenter.logout();
        verify(submissionsView, times(1)).showError(anyObject());
        verify(submissionsView, times(0)).showLogin();
    }

    @Test
    public void refresh() {
        presenter.refreshSubmissions(null);
        verify(dataManager, times(1)).clear();
        verify(dataManager, times(1)).downloadSubmissions();
        verify(submissionsView, times(1)).showSubmission(mockSubmissions.get(0).getSubmission());
        verifyProgressAndNoError();
    }

    @Test
    public void refreshSpecificSubreddit() {
        presenter.refreshSubmissions(subreddits.get(1));
        verify(dataManager, times(1)).clear();
        verify(dataManager, times(1)).downloadSubmissions();
        verify(submissionsView, times(0)).showSubmission(any(Submission.class));
        verifyProgressAndNoError();
    }
}