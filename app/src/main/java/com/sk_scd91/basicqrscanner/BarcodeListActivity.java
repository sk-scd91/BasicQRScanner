package com.sk_scd91.basicqrscanner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

    private static final int CAMERA_PERMISSION_CODE = 0;

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
                Intent imgPickIntent = new Intent(Intent.ACTION_PICK);
                imgPickIntent.setType("image/*");
                startActivityForResult(imgPickIntent, IMG_REQUEST_CODE);
            }
        });

        FloatingActionButton cameraFab = (FloatingActionButton) findViewById(R.id.camera_fab);
        cameraFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMG_REQUEST_CODE) {
            // TODO Scan barcodes from the image.
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
                    Toast.makeText(this, "Barcode detector not operational.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Frame frame = new Frame.Builder().setBitmap(image).build();
                SparseArray<Barcode> barcodes = detector.detect(frame);
                if (barcodes.size() > 0) {
                    Toast.makeText(this, barcodes.valueAt(0).rawValue, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "No QR codes found.", Toast.LENGTH_SHORT).show();
                }
                detector.release();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
