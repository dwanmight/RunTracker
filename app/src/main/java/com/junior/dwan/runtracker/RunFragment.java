package com.junior.dwan.runtracker;

import android.content.Context;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Might on 11.01.2017.
 */

public class RunFragment extends Fragment {
    private Button mStartButton, mStopButton;
    private TextView mStartedTextView, mLatitudeTextView, mLongitudeTextView, mAltitudeTextView, mDurationTextView;
    private RunManager mRunManager;
    private Run mRun;
    private Location mLastLocation;
    public static final String ARG_RUN_ID = "RUN_ID";

    public static RunFragment newInstance(long runId) {
        Bundle args = new Bundle();
        args.putLong(ARG_RUN_ID, runId);
        RunFragment runFragment = new RunFragment();
        runFragment.setArguments(args);
        return runFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mRunManager = RunManager.get(getActivity());

        // Проверить идентификатор Run и получить объект серии
        Bundle args = getArguments();
        if (args != null) {
            long runId = args.getLong(ARG_RUN_ID, -1);
            if (runId != -1) {
                mRun = RunManager.get(getActivity()).getRun(runId);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_run, container, false);

        mStartedTextView = (TextView) v.findViewById(R.id.run_startedTextView);
        mLatitudeTextView = (TextView) v.findViewById(R.id.run_latitudeTextView);
        mLongitudeTextView = (TextView) v.findViewById(R.id.run_longitudeTextView);
        mAltitudeTextView = (TextView) v.findViewById(R.id.run_altitudeTextView);
        mDurationTextView = (TextView) v.findViewById(R.id.run_durationTextView);

        mStartButton = (Button) v.findViewById(R.id.run_startButton);
        mStopButton = (Button) v.findViewById(R.id.run_stopButton);

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mRunManager.startLocationUpdates();
//                mRun = new Run();
                if (mRun == null) {
                    mRunManager.startNewRun();
                } else {
                    mRunManager.startTrackingRun(mRun);
                }
                updateUI();
            }
        });
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mRunManager.stopLocationUpdates();
                mRunManager.stopRun();
                updateUI();
            }
        });
        updateUI();
        return v;
    }

    private void updateUI() {
        boolean started = mRunManager.isTrackingRun();
        boolean trackingThisRun = mRunManager.isTrackingRun();
        mStartButton.setEnabled(!started);
//        mStopButton.setEnabled(started);
        mStopButton.setEnabled(started && trackingThisRun);
        if (mRun != null) {
            mStartedTextView.setText(mRun.getStartDate().toString());
            int durationSeconds = 0;
            if (mRun != null && mLastLocation != null) {
                durationSeconds = mRun.getDurationSeconds(mLastLocation.getTime());

                mLatitudeTextView.setText(Double.toString(mLastLocation.getLatitude()));
                mLongitudeTextView.setText(Double.toString(mLastLocation.getLongitude()));
                mAltitudeTextView.setText(Double.toString(mLastLocation.getAltitude()));
            }
            mDurationTextView.setText(Run.formatDuration(durationSeconds));
        }
    }

    private LocationReceiver myLocationReceiver = new LocationReceiver() {
        @Override
        protected void onLocationReceived(Context context, Location location) {
            if (mRunManager.isTrackingRun()) {
                return;
            }
            mLastLocation = location;
            if (isVisible())
                updateUI();
        }

        @Override
        protected void onProviderEnabledChanged(boolean enabled) {
            int toastEnabled = enabled ? R.string.gps_enabled : R.string.gps_disabled;
            Toast.makeText(getActivity(), toastEnabled, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(myLocationReceiver, new IntentFilter(RunManager.ACTION_LOCATION));
    }

    @Override
    public void onStop() {
        getActivity().unregisterReceiver(myLocationReceiver);
        super.onStop();
    }
}
