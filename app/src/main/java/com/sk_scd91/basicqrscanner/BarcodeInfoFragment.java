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

    private int mBarcodeType;
    private String mBarcodeDisplayValue;

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
                Barcode barcode = (Barcode) getArguments().getParcelable(ARG_BARCODE);
                mBarcodeType = barcode.valueFormat;
                mBarcodeDisplayValue = barcode.displayValue;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_barcode_info, container, false);

        TextView infoTypeView = (TextView) view.findViewById(R.id.barcode_info_type);
        infoTypeView.setText(getString(R.string.format_info_qr_type, getString(getNameOfBarcodeType())));

        TextView infoTextView = (TextView) view.findViewById(R.id.barcode_info_text);
        setAutoLinkForBarcodeType(infoTextView);
        infoTextView.setText(mBarcodeDisplayValue);

        return view;
    }

    // Return the string resource id for the barcode type name.
    private int getNameOfBarcodeType() {
        switch (mBarcodeType) {
            case Barcode.TEXT:
                return R.string.info_type_text;
            case Barcode.URL:
                return R.string.info_type_url;
            case Barcode.EMAIL:
                return R.string.info_type_email;
            case Barcode.PHONE:
                return R.string.info_type_phone;
            default:
                return R.string.info_type_unknown;
        }
    }

    // Set up the correct link type for common barcode types.
    private void setAutoLinkForBarcodeType(TextView textView) {
        switch (mBarcodeType) {
            case Barcode.URL:
                textView.setAutoLinkMask(Linkify.WEB_URLS);
                break;
            case Barcode.EMAIL:
                textView.setAutoLinkMask(Linkify.EMAIL_ADDRESSES);
                break;
            case Barcode.PHONE:
                textView.setAutoLinkMask(Linkify.PHONE_NUMBERS);
                break;
            default:
                textView.setAutoLinkMask(0);
        }
    }

}
