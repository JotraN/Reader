package io.github.jotran.reader.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fasterxml.jackson.databind.JsonNode;

import net.dean.jraw.models.Submission;
import net.dean.jraw.util.JrawUtils;

import java.util.ArrayList;
import java.util.List;

public class SubmissionDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "Submissions.db";
    private static final int DB_VERSION = 1;

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_TASKS_ENTRIES =
            "CREATE TABLE " + SubmissionsContract.SubmissionEntry.TABLE_NAME + " (" +
                    SubmissionsContract.SubmissionEntry._ID + " INTEGER PRIMARY KEY," +
                    SubmissionsContract.SubmissionEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    SubmissionsContract.SubmissionEntry.COLUMN_NAME_SUBREDDIT + TEXT_TYPE + COMMA_SEP +
                    SubmissionsContract.SubmissionEntry.COLUMN_NAME_SCORE + TEXT_TYPE + COMMA_SEP +
                    SubmissionsContract.SubmissionEntry.COLUMN_NAME_DATE_SUBMITTED + TEXT_TYPE + COMMA_SEP +
                    SubmissionsContract.SubmissionEntry.COLUMN_NAME_JSON + TEXT_TYPE + ");";
    private static final String SQL_DELETE_TASKS_ENTRIES = "DROP TABLE IF EXISTS " +
            SubmissionsContract.SubmissionEntry.TABLE_NAME;

    public SubmissionDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TASKS_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_TASKS_ENTRIES);
        db.execSQL(SQL_CREATE_TASKS_ENTRIES);
    }

    /**
     * Resets the given {@code SQLiteDatabase} to a new state.
     */
    public void reset() {
        SQLiteDatabase db = getReadableDatabase();
        db.execSQL(SQL_DELETE_TASKS_ENTRIES);
        db.execSQL(SQL_CREATE_TASKS_ENTRIES);
    }

    /**
     * Adds the given list of {@code Submission}s to the submissions database.
     *
     * @param submissions the list of {@code Submission}s to add
     */
    public void addSubmissions(List<Submission> submissions) {
        SQLiteDatabase db = getWritableDatabase();
        for (Submission submission : submissions) {
            addSubmission(db, submission);
        }
        db.close();
    }

    /**
     * Adds the given {@code Submission} to the given {@code SQLiteDatabase}.
     *
     * @param db         the {@code SQLiteDatabase} to add to
     * @param submission the {@code Submission} to add
     */
    private void addSubmission(SQLiteDatabase db, Submission submission) {
        ContentValues values = new ContentValues();
        values.put(SubmissionsContract.SubmissionEntry.COLUMN_NAME_TITLE, submission.getTitle());
        values.put(SubmissionsContract.SubmissionEntry.COLUMN_NAME_SUBREDDIT, submission.getSubredditName());
        values.put(SubmissionsContract.SubmissionEntry.COLUMN_NAME_SCORE, submission.getScore());
        values.put(SubmissionsContract.SubmissionEntry.COLUMN_NAME_DATE_SUBMITTED, submission.getCreated().toString());
        values.put(SubmissionsContract.SubmissionEntry.COLUMN_NAME_JSON, submission.getDataNode().toString());
        db.insert(SubmissionsContract.SubmissionEntry.TABLE_NAME, null, values);
    }

    /**
     * Gets the list of {@code Submission}s from the submissions database.
     *
     * @return the list of {@code Submission}s found in the submissions database
     */
    public List<Submission> getSubmissions() {
        SQLiteDatabase db = getReadableDatabase();
        List<Submission> submissions = getSubmissions(db, null);
        db.close();
        return submissions;
    }

    /**
     * Gets the list of {@code Submission}s from the given {@code SQLiteDatabase}.
     *
     * @param db        the {@code SQLiteDatabase} to get {@code Submission}s from
     * @param subreddit the subreddit to filter with
     * @return the list of {@code Submission}s found in the given {@code SQLiteDatabase}
     */
    public List<Submission> getSubmissions(SQLiteDatabase db, String subreddit) {
        List<Submission> submissions = new ArrayList<>();
        String[] projection = {SubmissionsContract.SubmissionEntry.COLUMN_NAME_JSON};
        String whereClause = SubmissionsContract.SubmissionEntry.COLUMN_NAME_SUBREDDIT +
                "=?";
        String[] whereArgs = {subreddit};
        if (subreddit == null) {
            whereClause = null;
            whereArgs = null;
        }
        String sortOrder = SubmissionsContract.SubmissionEntry._ID + " ASC";
        Cursor c = db.query(SubmissionsContract.SubmissionEntry.TABLE_NAME, projection, whereClause,
                whereArgs, null, null, sortOrder);
        while (c.moveToNext()) {
            int jsonColIndex = c.getColumnIndex(SubmissionsContract.SubmissionEntry.COLUMN_NAME_JSON);
            String json = c.getString(jsonColIndex);
            JsonNode jsonNode = JrawUtils.fromString(json);
            Submission submission = new Submission(jsonNode);
            submissions.add(submission);
        }
        c.close();
        return submissions;
    }

    /**
     * Gets the list of unique subreddits from the given {@code SQLiteDatabase}.
     *
     * @param db the {@code SQLiteDatabase} to get unique subreddits from
     * @return the list of unqiue subreddits found in the given {@code SQLiteDatabase}
     */
    public List<String> getSubreddits(SQLiteDatabase db) {
        List<String> subreddits = new ArrayList<>();
        String[] projection = {SubmissionsContract.SubmissionEntry.COLUMN_NAME_SUBREDDIT};
        String sortOrder = SubmissionsContract.SubmissionEntry.COLUMN_NAME_SUBREDDIT + " ASC";
        Cursor c = db.query(true, SubmissionsContract.SubmissionEntry.TABLE_NAME, projection, null,
                null, null, null, sortOrder, null);
        while (c.moveToNext()) {
            int subIndex = c.getColumnIndex(SubmissionsContract.SubmissionEntry.COLUMN_NAME_SUBREDDIT);
            subreddits.add(c.getString(subIndex));
        }
        c.close();
        return subreddits;
    }
}
