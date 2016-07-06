package io.github.jotran.reader.presenter;

import android.content.Context;

import io.github.jotran.reader.model.DataManager;

/**
 * Base presenter used by the Reader app.
 * <p>
 * Requires the implementation of authenticate, since all presenters need to authenticate the
 * client before using it.
 */
public abstract class BasePresenter {
    protected DataManager mDataManager;
    protected Context mContext;

    public BasePresenter(Context context) {
        mContext = context;
        mDataManager = new DataManager(context);
    }

    /**
     * Authenticates the client.
     */
    public abstract void authenticate();
}