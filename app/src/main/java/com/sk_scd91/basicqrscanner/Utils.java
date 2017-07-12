package com.sk_scd91.basicqrscanner;

import android.text.util.Linkify;
import android.widget.TextView;

import com.google.android.gms.vision.barcode.Barcode;

/**
 * (c) 2017 Sean Deneen
 */

public final class Utils {

    // Return the string resource id for the barcode type name.
    public static int getNameOfBarcodeType(int barcodeType) {
        switch (barcodeType) {
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
    public static void setAutoLinkForBarcodeType(TextView textView, int barcodeType) {
        switch (barcodeType) {
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
