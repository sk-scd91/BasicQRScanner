package com.sk_scd91.basicqrscanner;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.vision.barcode.Barcode;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BarcodeInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BarcodeInfoFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_BARCODE = "barcode";

    // TODO: Rename and change types of parameters
    private int mBarcodeType;
    private String mBarcodeDisplayValue;

    public BarcodeInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_barcode_info, container, false);

        TextView infoTypeView = (TextView) view.findViewById(R.id.barcode_info_type);
        infoTypeView.setText(getString(R.string.format_info_qr_type, getString(getNameOfBarcodeType())));

        TextView infoTextView = (TextView) view.findViewById(R.id.barcode_info_text);
        infoTextView.setText(mBarcodeDisplayValue);

        return view;
    }

    private int getNameOfBarcodeType() {
        switch (mBarcodeType) {
            case Barcode.TEXT:
                return R.string.info_type_text;
            case Barcode.URL:
                return R.string.info_type_url;
            default:
                return R.string.info_type_unknown;
        }
    }

}
