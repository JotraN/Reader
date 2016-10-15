package io.github.jotran.reader.presenter;

import android.content.Context;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import io.github.jotran.reader.model.DataManager;
import io.github.jotran.reader.util.RxSchedulersOverrideRule;
import rx.Observable;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class LoginPresenterTest {
    @Rule
    public final RxSchedulersOverrideRule overrideSchedulersRule = new RxSchedulersOverrideRule();

    @Mock
    private LoginPresenter.LoginView loginView;

    @Mock
    private DataManager dataManager;

    private LoginPresenter presenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        presenter = new LoginPresenter(Mockito.mock(Context.class), loginView, dataManager);
    }

    @Test
    public void loginWhenNoRefreshToken() {
        String authUrl = "a url";
        when(dataManager.getAuthUrl()).thenReturn(authUrl);
        presenter.authenticate();
        verify(dataManager, times(1)).clear();
        verify(dataManager, times(0)).isAuthenticated();
        verify(dataManager, times(0)).authenticateUrl(any(String.class));
        verify(dataManager, times(0)).authenticateToken(any(String.class));
        verify(loginView, times(0)).showAuthenticated();
        verify(loginView, times(1)).showLogin(authUrl);
    }

    @Test
    public void authenticateWithUrl() {
        String authUrl = "a url";
        when(dataManager.authenticateUrl(authUrl)).thenReturn(Observable.just(authUrl));
        presenter.authenticate(authUrl);
        verify(dataManager, times(0)).clear();
        verify(dataManager, times(0)).isAuthenticated();
        verify(dataManager, times(1)).authenticateUrl(authUrl);
        verify(dataManager, times(0)).authenticateToken(any(String.class));
        verify(loginView, times(1)).showAuthenticated();
        verify(loginView, times(0)).showLogin(authUrl);
    }
}
