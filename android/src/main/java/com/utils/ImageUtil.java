package com.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import java.io.ByteArrayOutputStream;

public class ImageUtil
{
    public static Bitmap convert(String base64Str) throws IllegalArgumentException
    {
        byte[] decodedBytes = Base64.decode(
            base64Str.substring(base64Str.indexOf(",")  + 1),
            Base64.DEFAULT
        );

        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public static byte[] getByteArray(Bitmap bitmap)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

        return outputStream.toByteArray();
    }


    public static String convert(Bitmap bitmap)
    {
        return Base64.encodeToString(getByteArray(bitmap), Base64.DEFAULT);
    }
    public static String convert(byte[] array)
    {
        return Base64.encodeToString(array, Base64.DEFAULT);
    }

}