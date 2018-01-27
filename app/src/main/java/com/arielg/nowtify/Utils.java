package com.arielg.nowtify;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.VectorDrawable;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.io.InputStream;

class Utils {

    private static TypedArray mStockIcons = null;

    //===================================================================
    public static Bitmap getStockIconBitmapAt(Resources r, int idx) {

        if(mStockIcons == null) {
            mStockIcons = r.obtainTypedArray(R.array.nowtify_stock_icons);
        }
        return getBitmapFromVectorDrawable((VectorDrawable)r.getDrawable(mStockIcons.getResourceId(idx, 0), null));
    }

    //===================================================================
    public static Bitmap getStockIconBitmap(Resources r, int resId) {
        return getBitmapFromVectorDrawable((VectorDrawable)r.getDrawable(resId, null));
    }

    //===================================================================
    public static Bitmap getBitmapFromVectorDrawable(VectorDrawable d) {
        Bitmap bm = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        d.draw(canvas);
        return bm;
    }

    //===================================================================
    public static int devicePixelsToPixels(int devicePixels, DisplayMetrics displayMetrics) {
        return (int)(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, devicePixels, displayMetrics));
    }

    //===================================================================
    public static Bitmap getThumbnail(Context context, Uri uri) throws Exception {

        InputStream input = context.getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();

        //onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional

        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1))
            return null;

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;
        int dimen = context.getResources().getDimensionPixelOffset(R.dimen.image_icon_dimens);

        double ratio = (originalSize > dimen) ? (originalSize / dimen) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

        //bitmapOptions.inDither=true;//optional
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional

        input = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();

        return bitmap;
    }

    //////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////
    public static void verifyReadContactsPermissions(Activity activity) {

        // Storage Permissions
        final int REQUEST_READ_CONTACTS = 1;
        String[] PERMISSIONS_CONTACTS = {
                Manifest.permission.READ_CONTACTS
        };
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_CONTACTS,
                    REQUEST_READ_CONTACTS
            );
        }
    }

    //===================================================================
    private static int getPowerOfTwoForSampleRatio(double ratio) {
        int k = Integer.highestOneBit((int)Math.floor(ratio));
        if(k==0)
            return 1;
        else
            return k;
    }
}
