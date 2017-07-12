package com.sk_scd91.basicqrscanner;

/**
 *
 * (c) 2017 Sean Deneen
 *
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
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
public class BarcodeInfoFragment extends Fragment {

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_barcode_info, container, false);

        TextView infoTypeView = (TextView) view.findViewById(R.id.barcode_info_type);
        infoTypeView.setText(getString(R.string.format_info_qr_type,
                getString(Utils.getNameOfBarcodeType(mBarcode.valueFormat))));

        TextView infoTextView = (TextView) view.findViewById(R.id.barcode_info_text);
        Utils.setAutoLinkForBarcodeType(infoTextView, mBarcode.valueFormat);
        infoTextView.setText(mBarcode.displayValue);

        return view;
    }

}
