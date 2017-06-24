package com.sk_scd91.basicqrscanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class CameraScanActivity extends AppCompatActivity {

    private static final String TAG = "CameraScanActivity";

    public static String EXTRA_BARCODE = "extra_barcode";

    private SurfaceView mCameraView;
    private CameraSource mCameraSource;

    private boolean mCanAutoFocus;
    private boolean mStartingCamera = false;
    private boolean mSurfaceExists = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_scan);

        mCameraView = (SurfaceView) findViewById(R.id.camera_surface_view);
        mCanAutoFocus = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS);
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
    }

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
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setAutoFocusEnabled(mCanAutoFocus)
                .setRequestedFps(15f)
                .build();
    }

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
