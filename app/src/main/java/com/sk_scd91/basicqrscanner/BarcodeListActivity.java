package com.sk_scd91.basicqrscanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.io.InputStream;

public class BarcodeListActivity extends AppCompatActivity {

    private static final String TAG = "BarcodeListActivity";

    private static final int IMG_REQUEST_CODE = 0x10;
    private static final int CAMERA_REQUEST_CODE = 0x11;

    private static final int CAMERA_PERMISSION_CODE = 0x10;
    private static final int VIBRATE_PERMISSION_CODE = 0x20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton imageFab = (FloatingActionButton) findViewById(R.id.img_fab);
        imageFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchImagePicker();
            }
        });

        FloatingActionButton cameraFab = (FloatingActionButton) findViewById(R.id.camera_fab);
        cameraFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });

        requestVibrateFeature();
    }

    private void launchImagePicker() {
        Intent imgPickIntent = new Intent(Intent.ACTION_PICK);
        imgPickIntent.setType("image/*");
        startActivityForResult(imgPickIntent, IMG_REQUEST_CODE);
    }

    private void launchCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startActivityForResult(new Intent(this, CameraScanActivity.class), CAMERA_REQUEST_CODE);
        } else {
            Log.d(TAG, "Requesting camera permission");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
        }
    }

    private void requestVibrateFeature() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.VIBRATE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Requesting vibrate permission");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.VIBRATE},
                    VIBRATE_PERMISSION_CODE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_barcode_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "Requesting permissions.");
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "CAMERA permission granted.");
                launchCamera();
            } else {
                Toast.makeText(this, R.string.error_camera_permission_denied, Toast.LENGTH_SHORT);
            }
            return;
        } else if (requestCode == VIBRATE_PERMISSION_CODE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "VIBRATE permission granted.");
            } else {
                Toast.makeText(this, R.string.error_vibrate_permission_denied, Toast.LENGTH_SHORT);
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMG_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "Found Image :" + data);

                Bitmap image = null;

                try {
                    InputStream imageInput = getContentResolver().openInputStream(data.getData());
                    image = BitmapFactory.decodeStream(imageInput);
                    imageInput.close();
                } catch (IOException e) {
                    Log.e(TAG, "Unable to use image file.", e);
                    return;
                }

                BarcodeDetector detector = new BarcodeDetector.Builder(this)
                        .setBarcodeFormats(Barcode.QR_CODE)
                        .build();
                if (!detector.isOperational()) {
                    Log.e(TAG, "Barcode detector not operational.");
                    Toast.makeText(this, R.string.error_barcode_detector_not_operational, Toast.LENGTH_SHORT).show();
                    return;
                }
                Frame frame = new Frame.Builder().setBitmap(image).build();
                SparseArray<Barcode> barcodes = detector.detect(frame);
                if (barcodes.size() > 0) {
                    Barcode barcode = barcodes.valueAt(0);
                    replaceMainFragment(BarcodeInfoFragment.newInstance(barcode));
                    Toast.makeText(this, barcode.rawValue, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, R.string.error_no_qr_found, Toast.LENGTH_SHORT).show();
                }
                detector.release();
            } else {
                Log.d(TAG, "Image request cancelled.");
            }
            return;
        } else if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null &&
                        data.getParcelableExtra(CameraScanActivity.EXTRA_BARCODE) != null) {
                    replaceMainFragment(BarcodeInfoFragment
                            .newInstance((Barcode) data.getParcelableExtra(CameraScanActivity.EXTRA_BARCODE)));
                } else {
                    Log.d(TAG, "No barcode data.");
                }
            } else {
                Log.d(TAG, "Barcode request cancelled.");
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void replaceMainFragment(Fragment newFragment) {
        getSupportFragmentManager().beginTransaction()
                .addToBackStack(newFragment.getClass().getSimpleName())
                .replace(R.id.fragment_container, newFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }
}
