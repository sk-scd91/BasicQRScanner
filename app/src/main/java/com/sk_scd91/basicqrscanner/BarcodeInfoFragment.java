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

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.vision.barcode.Barcode;


/**
 * A {@link Fragment} subclass that displays the type and text of the barcode.
 * Use the {@link BarcodeInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BarcodeInfoFragment extends AppCompatDialogFragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_BARCODE = "barcode";

    private Barcode mBarcode;

    public BarcodeInfoFragment() {
    }

    /**
     * Factory method to instantiate a {@link BarcodeInfoFragment} with a {@link Barcode}.
     *
     * @param barcode Barcode data to display.
     * @return A new instance of fragment BarcodeInfoFragment.
     */
    public static BarcodeInfoFragment newInstance(Barcode barcode) {
        BarcodeInfoFragment fragment = new BarcodeInfoFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_BARCODE, barcode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().getParcelable(ARG_BARCODE) != null) {
                mBarcode = (Barcode) getArguments().getParcelable(ARG_BARCODE);
            }
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext())
                .setTitle("QR Code Scanned: ")
                .setView(R.layout.fragment_barcode_info)
                .setPositiveButton(R.string.info_dialog_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((BarcodeListActivityFragment)getFragmentManager().findFragmentById(R.id.fragment))
                                .setNewBarcode(mBarcode);
                    }
                }).setNegativeButton(R.string.info_dialog_cancel, null);

        AlertDialog dialog = dialogBuilder.create();
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        setUpDialogText(getDialog());
    }

    private void setUpDialogText(Dialog dialog) {
        TextView infoTypeView = (TextView) dialog.findViewById(R.id.barcode_info_type);
        infoTypeView.setText(getString(R.string.format_info_qr_type,
                getString(Utils.getNameOfBarcodeType(mBarcode.valueFormat))));

        TextView infoTextView = (TextView) dialog.findViewById(R.id.barcode_info_text);
        Utils.setAutoLinkForBarcodeType(infoTextView, mBarcode.valueFormat);
        infoTextView.setText(mBarcode.displayValue);
    }

}
