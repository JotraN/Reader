package io.github.jotran.reader.model;

import android.provider.BaseColumns;

public final class SubmissionsContract {
    public SubmissionsContract(){}

    public static abstract class SubmissionEntry implements BaseColumns {
        public static final String TABLE_NAME = "submissions";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_SUBREDDIT = "subreddit";
        public static final String COLUMN_NAME_SCORE = "score";
        public static final String COLUMN_NAME_DATE_SUBMITTED = "date_submitted";
        public static final String COLUMN_NAME_JSON = "json";
    }
}
