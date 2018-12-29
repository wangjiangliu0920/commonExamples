package com.icecold.sleepbandtest.utils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 *
 * Created by icecold_laptop_2 on 2018/8/2.
 */

public class ParcelableUtil {

    public static byte[] marshall(Parcelable parceable) {
        Parcel parcel = Parcel.obtain();
        parcel.setDataPosition(0);
        parceable.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();

//        ViseLog.d("bytes = " + Arrays.toString(bytes) + "parcel" + parcel.toString());
        parcel.recycle();
        return bytes;
    }

    public static Parcel unmarshall(byte[] bytes) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0);
        return parcel;
    }
}
