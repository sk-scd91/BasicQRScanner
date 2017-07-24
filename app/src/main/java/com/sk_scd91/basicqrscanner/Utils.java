package com.sk_scd91.basicqrscanner;

import android.text.util.Linkify;
import android.widget.TextView;

import com.google.android.gms.vision.barcode.Barcode;

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

/**
 * Miscellaneous view utilities.
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
