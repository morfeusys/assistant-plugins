package com.example.plugin.tasker;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import java.util.Map;

import static mobi.voiceassistant.base.AgentContract.Content.COLUMN_ID;
import static mobi.voiceassistant.base.AgentContract.Content.COLUMN_VALUE;
import static mobi.voiceassistant.base.AgentContract.Content.FUZZY_KEY_VALUE_TYPE;

/**
 * Created by morfeusys on 01.12.13.
 */
public class TaskContentProvider extends ContentProvider {

    public static final String PREFERENCES = "com.example.plugin.tasker.commands";
    public static final String AUTHORITY = "com.example.plugin.tasker";
    public static final String PATH_COMMANDS = "commands";

    private static final int COMMANDS = 1;

    private UriMatcher mMatcher;

    @Override
    public boolean onCreate() {
        mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mMatcher.addURI(AUTHORITY, PATH_COMMANDS, COMMANDS);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SharedPreferences preferences = getContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        Map<String, ?> map = preferences.getAll();
        MatrixCursor cursor = new MatrixCursor(new String[] {COLUMN_ID, COLUMN_VALUE});
        for(String key : map.keySet()) {
            String value = (String) map.get(key);
            cursor.addRow(new String[] {value, key});
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (mMatcher.match(uri)) {
            case COMMANDS:
                return FUZZY_KEY_VALUE_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
