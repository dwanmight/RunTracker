package com.junior.dwan.runtracker;

import android.support.v4.app.Fragment;

/**
 * Created by Might on 13.01.2017.
 */

public class RunListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new RunListFragment();
    }
}
