package com.sk_scd91.basicqrscanner;

/**
 * Copyright 2017 Sean Deneen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

/**
 * An {@link AppCompatActivity} that displays a camera preview and scans barcodes.
 * Uses Google Play mobile vision for the QR code scanner.
 */
public class CameraScanActivity extends AppCompatActivity {

    private static final String TAG = "CameraScanActivity";

    public static String EXTRA_BARCODE = "extra_barcode";

    private SurfaceView mCameraView;
    private CameraSource mCameraSource;

    private boolean mCanAutoFocus;
    private int mCameraFacing;
    private float mCameraFPS;
    private boolean mStartingCamera = false;
    private boolean mSurfaceExists = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_scan);

        mCameraView = (SurfaceView) findViewById(R.id.camera_surface_view);

        // Get the preferences from settings.
        PreferenceManager.setDefaultValues(this, R.xml.camera_prefs, false);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        mCanAutoFocus = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS)
                && sp.getBoolean(getString(R.string.pref_key_autofocus_preference), true);
        mCameraFacing = sp.getBoolean(getString(R.string.pref_key_face_preference), false)
                ? CameraSource.CAMERA_FACING_FRONT : CameraSource.CAMERA_FACING_BACK;
        // Get the string preference as a float.
        try {
            mCameraFPS = Float.parseFloat(sp.getString(getString(R.string.pref_key_camera_fps), "15"));
        } catch (NumberFormatException e) {
            mCameraFPS = 15f;
        }

        createCameraSource();

        // Detect when a Surface is created.
        mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d(TAG, "Surface created.");
                mSurfaceExists = true;
                tryStartCamera();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d(TAG, "Surface destroyed.");
                mSurfaceExists = false;
            }
        });

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
            getWindow().getDecorView()
                    .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    Log.d("TAG", "System UI Visibility " + visibility);
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        actionBar.show();
                    } else {
                        actionBar.hide();
                    }
                }
            });
        }

        if (savedInstanceState == null) { // Only show when launched, not recreated.
            Toast.makeText(this, R.string.scan_directions, Toast.LENGTH_LONG).show();
        }
    }

    // Create a camera source with a barcode detector that retrieves the first barcode detected.
    private void createCameraSource() {
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        if (!barcodeDetector.isOperational()) {
            Log.e(TAG, "Barcode detector is not operational.");
            return;
        }

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                SparseArray<Barcode> foundBarcodes = detections.getDetectedItems();
                if (foundBarcodes.size() > 0)
                    sendBarcodeAsResult(foundBarcodes.valueAt(0));
            }
        });

        mCameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setFacing(mCameraFacing)
                .setAutoFocusEnabled(mCanAutoFocus)
                .setRequestedFps(mCameraFPS)
                .build();
    }

    // Finish the activity with the given barcode as the result.
    // Also, optionally vibrate for feedback.
    private void sendBarcodeAsResult(Barcode barcode) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.VIBRATE)
                == PackageManager.PERMISSION_GRANTED) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator())
                vibrator.vibrate(50L);
        }

        final Intent barcodeResult = new Intent();
        barcodeResult.putExtra(EXTRA_BARCODE, barcode);
        setResult(RESULT_OK, barcodeResult);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensure Google Play Services are available.
        GoogleApiAvailability gpAvalability = GoogleApiAvailability.getInstance();
        int availCode = gpAvalability.isGooglePlayServicesAvailable(this);
        if (availCode != ConnectionResult.SUCCESS) {
            gpAvalability.getErrorDialog(this, availCode, 0).show();
            return;
        }

        Log.d(TAG, "Camera is resuming.");
        mStartingCamera = true;
        tryStartCamera();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideStatusBar();
        }
    }

    public void hideStatusBar() {
        //Hide status bar.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Start the camera when the activity is resuming and the SurfaceView's Surface is available.
    private void tryStartCamera() {
        if (mStartingCamera && mSurfaceExists && mCameraSource != null) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED)
                    mCameraSource.start(mCameraView.getHolder());
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera", e);
                mCameraSource.release();
                mCameraSource = null;
            } finally {
                mStartingCamera = false;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mCameraSource != null)
            mCameraSource.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mCameraSource != null)
            mCameraSource.release();
    }
}
