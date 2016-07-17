package io.github.jotran.reader.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.github.jotran.reader.R;


public class SplashFragment extends Fragment {

    private SplashFragmentListener mListener;

    public interface SplashFragmentListener {
        void onLoginBtnPressed();
    }

    public SplashFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_splash, container, false);
        v.setOnClickListener(view -> {
            if (mListener != null)
                mListener.onLoginBtnPressed();
        });
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SplashFragmentListener) {
            mListener = (SplashFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement SplashFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}