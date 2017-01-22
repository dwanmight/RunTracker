package com.junior.dwan.runtracker;

import android.support.v4.app.Fragment;

public class RunActivity extends SingleFragmentActivity {
    public static final String TAG_PREFIX = "PREFIX ";
    /**
     * Ключ для передачи идентификатора серии в формате long
     */
    public static final String EXTRA_RUN_ID = "com.junior.dwan.runtracker.RUN_ID";

    @Override
    protected Fragment createFragment() {
        long runId = getIntent().getLongExtra(EXTRA_RUN_ID, -1);
        if (runId != -1) {
            return RunFragment.newInstance(runId);
        }
        return new RunFragment();
    }
}
