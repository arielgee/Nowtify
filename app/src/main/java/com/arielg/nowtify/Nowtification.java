package com.arielg.nowtify;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

public class Nowtification implements Serializable {

    private int mId;
    private int mIconIndex;
    private byte mIconBitmapBytes[];
    private String mTitle;
    private String mContent;
    private long mWhen;
    private boolean mOngoing;


    private Nowtification() {
    }

    public Nowtification(int mIconIndex, Bitmap bitmap, String mTitle, String mContent) {
        this.mId = ((int)SystemClock.elapsedRealtimeNanos()) & Integer.MAX_VALUE;       // covert the long value to a positive integer
        this.mIconIndex = mIconIndex;
        setIconBitmapBytes(bitmap);
        this.mTitle = mTitle;
        this.mContent = mContent;
        this.mWhen = new Date().getTime();
        this.mOngoing = true;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public int getIconIndex() {
        return mIconIndex;
    }

    public Bitmap getIconBitmap() {
        return getIconBitmapBytes();
    }

    public String getTitle() {
        return mTitle;
    }

    public String getContent() {
        return mContent;
    }

    public long getWhen() {
        return mWhen;
    }

    public boolean isOngoing() {
        return mOngoing;
    }

    ////////////////////////////////////////////////////////////////////
    ///
    public void cloneData(Nowtification n) {
        if(this != n) {
            this.mIconIndex = n.mIconIndex;
            this.mIconBitmapBytes = n.mIconBitmapBytes;
            this.mTitle = n.mTitle;
            this.mContent = n.mContent;
            this.mWhen = n.mWhen;
            this.mOngoing = n.mOngoing;
        }
    }

    ////////////////////////////////////////////////////////////////////
    ///
    public void write(ObjectOutputStream out) throws IOException {

        if (out == null)
            return;

        out.writeInt(mId);
        out.writeInt(mIconIndex);
        out.writeInt(mIconBitmapBytes.length);
        out.write(mIconBitmapBytes, 0, mIconBitmapBytes.length);
        out.writeObject(mTitle);
        out.writeObject(mContent);
        out.writeLong(mWhen);
        out.writeBoolean(mOngoing);
    }

    ////////////////////////////////////////////////////////////////////
    ///
    public static Nowtification read(ObjectInputStream in) throws IOException, ClassNotFoundException {

        if (in == null)
            return null;

        Nowtification n = new Nowtification();

        n.mId = in.readInt();
        n.mIconIndex = in.readInt();
        n.mIconBitmapBytes = new byte[in.readInt()];
        in.readFully(n.mIconBitmapBytes, 0, n.mIconBitmapBytes.length);
        n.mTitle = (String) in.readObject();
        n.mContent = (String) in.readObject();
        n.mWhen = in.readLong();
        n.mOngoing = in.readBoolean();

        return n;
    }

    ////////////////////////////////////////////////////////////////////
    ///
    private void setIconBitmapBytes(Bitmap bitmap) {

        if (bitmap == null) {
            mIconBitmapBytes = new byte[0];
        } else {
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            mIconBitmapBytes = new byte[stream.size()];
            mIconBitmapBytes = stream.toByteArray();
        }
    }

    ////////////////////////////////////////////////////////////////////
    ///
    private Bitmap getIconBitmapBytes() {

        if (mIconBitmapBytes == null || mIconBitmapBytes.length == 0)
            return null;
        else
            return BitmapFactory.decodeByteArray(mIconBitmapBytes, 0, mIconBitmapBytes.length);
    }
}
