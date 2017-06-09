package no.nordicsemi.android.nrftoolbox.dfu;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;
import no.nordicsemi.android.nrftoolbox.R;

/**
 * Created by chendong on 2017/4/21.
 */
public class MyDfuAct extends Activity {

    private TextView mTextPercentage;
    private TextView mTextUploading;
    private ProgressBar mProgressBar;


    private final DfuProgressListener mDfuProgressListener = new DfuProgressListenerAdapter() {
        @Override
        public void onDeviceConnecting(final String deviceAddress) {
            mProgressBar.setIndeterminate(true);
            mTextPercentage.setText(R.string.dfu_status_connecting);
        }

        @Override
        public void onDfuProcessStarting(final String deviceAddress) {
            mProgressBar.setIndeterminate(true);
            mTextPercentage.setText(R.string.dfu_status_starting);
        }

        @Override
        public void onEnablingDfuMode(final String deviceAddress) {
            mProgressBar.setIndeterminate(true);
            mTextPercentage.setText(R.string.dfu_status_switching_to_dfu);
        }

        @Override
        public void onFirmwareValidating(final String deviceAddress) {
            mProgressBar.setIndeterminate(true);
            mTextPercentage.setText(R.string.dfu_status_validating);
        }

        @Override
        public void onDeviceDisconnecting(final String deviceAddress) {
            mProgressBar.setIndeterminate(true);
            mTextPercentage.setText(R.string.dfu_status_disconnecting);
        }

        @Override
        public void onDfuAborted(final String deviceAddress) {
            mTextPercentage.setText(R.string.dfu_status_aborted);
            // let's wait a bit until we cancel the notification. When canceled immediately it will be recreated by service again.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mTextPercentage.setVisibility(View.INVISIBLE);
                    mTextUploading.setVisibility(View.INVISIBLE);
                    // if this activity is still open and upload process was completed, cancel the notification
                    final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(DfuThread.NOTIFICATION_ID);
                }
            }, 200);
        }

        @Override
        public void onDfuCompleted(final String deviceAddress) {
            showToast(R.string.dfu_success);
            mTextPercentage.setText(R.string.dfu_status_completed);
            // let's wait a bit until we cancel the notification. When canceled immediately it will be recreated by service again.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
//                    onTransferCompleted();
                    // if this activity is still open and upload process was completed, cancel the notification
                    final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(DfuThread.NOTIFICATION_ID);
                }
            }, 200);
        }

        @Override
        public void onProgressChanged(final String deviceAddress, final int percent, final float speed, final float avgSpeed, final int currentPart, final int partsTotal) {
            mProgressBar.setIndeterminate(false);
            mProgressBar.setProgress(percent);
            mTextPercentage.setText(getString(R.string.dfu_uploading_percentage, percent));
            if (partsTotal > 1)
                mTextUploading.setText(getString(R.string.dfu_status_uploading_part, currentPart, partsTotal));
            else
                mTextUploading.setText(R.string.dfu_status_uploading);
        }

        @Override
        public void onError(final String deviceAddress, final int error, final int errorType, final String message) {
//            showErrorMessage(message);
            // We have to wait a bit before canceling notification. This is called before DfuThread creates the last notification.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // if this activity is still open and upload process was completed, cancel the notification
                    final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(DfuThread.NOTIFICATION_ID);
                }
            }, 200);
        }
    };

        @Override
        protected void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.act_dfu);
            mTextPercentage = (TextView) findViewById(R.id.textviewProgress);
            mTextUploading = (TextView) findViewById(R.id.textviewUploading);
            mProgressBar = (ProgressBar) findViewById(R.id.progressbar_file);
        }

    @Override
    protected void onResume() {
        super.onResume();
        DfuServiceListenerHelper.registerProgressListener(this, mDfuProgressListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DfuServiceListenerHelper.unregisterProgressListener(this, mDfuProgressListener);
    }

    /**
     * Callback of UPDATE/CANCEL button on DfuActivity
     */
    public void onCDUploadClicked(final View view) {
        showProgressBar();
        final DfuServiceInitiator starter = new DfuServiceInitiator("C1:3C:F7:8F:79:90").setKeepBond(false);
        starter.setZip(R.raw.r02_update_wh0418);
        DfuThread thread = new DfuThread();
        thread.context  = this;
        starter.start(this, thread);
    }


    /**
     * Callback of CANCEL button on DfuActivity
     */
    public void onCancelClicked(final View view) {
        final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        final Intent pauseAction = new Intent(DfuThread.BROADCAST_ACTION);
        pauseAction.putExtra(DfuThread.EXTRA_ACTION, DfuThread.ACTION_ABORT);
        manager.sendBroadcast(pauseAction);
        mProgressBar.setIndeterminate(true);
        mTextUploading.setText(R.string.dfu_status_aborting);
        mTextPercentage.setText(null);
    }



    private void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
        mTextPercentage.setVisibility(View.VISIBLE);
        mTextPercentage.setText(null);
        mTextUploading.setText(R.string.dfu_status_uploading);
        mTextUploading.setVisibility(View.VISIBLE);
    }
    private void showToast(final int messageResId) {
        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();
    }
}
