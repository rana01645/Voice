package de.ph1b.audiobook.activity;


import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import de.ph1b.audiobook.R;
import de.ph1b.audiobook.utils.CommonTasks;

public class BookChoose extends ActionBarActivity {

    private static final String TAG = "de.ph1b.audiobook.activity.BookChoose";
    public static final String PLAY_BOOK = TAG + ".PLAY_BOOK";
    public static final String SHARED_PREFS = TAG + ".SHARED_PREFS";
    public static final String SHARED_PREFS_CURRENT = TAG + ".SHARED_PREFS_CURRENT";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_choose);
    }

    @Override
    public void onResume() {
        CommonTasks.checkExternalStorage(this);
        super.onResume();
    }
}
