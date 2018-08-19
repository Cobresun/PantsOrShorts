package com.cobresun.brun.pantsorshorts.presenter;

import com.cobresun.brun.pantsorshorts.repositories.UserDataRepository;
import com.cobresun.brun.pantsorshorts.view.MainActivityView;

import junit.framework.Assert;

import org.junit.Test;

public class MainActivityPresenterTest {

    @Test
    public void shouldPassUserPrefToView() {

        // given
        MainActivityView view = new MockView();
        UserDataRepository userDataRepository = new UserDataRepository() {
            @Override
            public float readUserThreshold() {
                return 0;
            }
        };

        // when
        MainActivityPresenter presenter = new MainActivityPresenter(view, userDataRepository);
        presenter.loadUserThreshold();

        // then
        Assert.assertEquals(true, ((MockView) view).displayUserThresholdWithThreshold21Called);
    }

    private class MockView implements MainActivityView {

        boolean displayUserThresholdWithThreshold21Called;

        @Override
        public void displayUserThreshold(float userThreshold) {
            if (userThreshold == 21f) displayUserThresholdWithThreshold21Called = true;
        }
    }
}