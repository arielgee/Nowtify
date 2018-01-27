package com.arielg.nowtify;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;

import java.util.ArrayList;

public class IconPicker extends GridLayout implements View.OnClickListener {

    public static final int BK_COLOR_ICON_DEFAULT = R.color.silver;
    private static final int BK_COLOR_ICON_SELECTED = R.color.darkOrange;
    private static final int BK_COLOR_ICON_UNSELECTED = BK_COLOR_ICON_DEFAULT;


    public interface IconPickerListener {
        void onIconSelectedListener();
    }

    private IconPickerListener mListener;

    private final int ARRAY_CAPACITY = 50;
    private final int IMAGE_VIEW_ID_RANGE = 9000;

    private GridLayout.LayoutParams mLayoutParams;
    private String mContentDescription;
    private int mPxIconDimension;
    private int mPxIconMargin;
    private ImageView.ScaleType mScaleType = ImageView.ScaleType.CENTER;

    private int mImageViewIdCounter;
    private int mSelectedImageView;
    private ArrayList<ImageView> mImageViews = null;

    /*****************************************************************/
    /***/
    public IconPicker(Context context) {
        super(context);
        init();
    }

    /*****************************************************************/
    /***/
    public IconPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /*****************************************************************/
    /***/
    public IconPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /*****************************************************************/
    /***/
    public void setIconSelectedListener(IconPickerListener listener) {
        mListener = listener;
    }

    /*****************************************************************/
    /***/
    public void setIconScaleTypeCenterCrop(boolean bCenterCrop) {
        mScaleType = (bCenterCrop ? ImageView.ScaleType.CENTER_CROP : ImageView.ScaleType.CENTER);
    }

    /*****************************************************************/
    /***/
    @Override
    public void onClick(View v) {
        // the tag holds the index of the view in the ArrayList.
        this.setSelectedIndex((Integer)(v.getTag()));

        if(mListener != null)
            mListener.onIconSelectedListener();
    }

    /*****************************************************************/
    /***/
    public int getSize() {
        return mImageViews.size();
    }

    /*****************************************************************/
    /***/
    public ImageView getSelectedIcon() {
        return mImageViews.get(mSelectedImageView);
    }

    /*****************************************************************/
    /***/
    public int getSelectedIndex() {
        return mSelectedImageView;
    }

    /*****************************************************************/
    /***/
    public void setSelectedIndex(int selected) {
        // restore none selected background color
        mImageViews.get(mSelectedImageView).setBackgroundResource(BK_COLOR_ICON_UNSELECTED);
        mSelectedImageView = selected;
        mImageViews.get(mSelectedImageView).setBackgroundResource(BK_COLOR_ICON_SELECTED);
    }


    /*****************************************************************/
    /***/
    public Bitmap getIcon() {
        ImageView iv = mImageViews.get(mSelectedImageView);

        if(iv == null)
            return null;

        return ((BitmapDrawable)iv.getDrawable()).getBitmap();
    }

    /*****************************************************************/
    /***/
    public void setIcon(Bitmap bitmap) {

        mImageViewIdCounter++;

        ImageView iv = new ImageView(getContext());

        iv.setId(IMAGE_VIEW_ID_RANGE + mImageViewIdCounter);
        iv.setScaleType(mScaleType);
        iv.setContentDescription(mContentDescription);
        iv.setImageBitmap(bitmap);
        iv.setBackgroundResource(BK_COLOR_ICON_UNSELECTED);
        iv.setOnClickListener(this);

        if(mScaleType == ImageView.ScaleType.CENTER_CROP)
            iv.setPadding(mPxIconMargin, mPxIconMargin, mPxIconMargin, mPxIconMargin);

        mLayoutParams = new GridLayout.LayoutParams();
        mLayoutParams.height = mLayoutParams.width = mPxIconDimension;
        mLayoutParams.setMargins(mPxIconMargin, mPxIconMargin, mPxIconMargin, mPxIconMargin);

        addView(iv, mLayoutParams);

        // the tag holds the index of the view in the ArrayList.
        iv.setTag(mImageViews.size());
        mImageViews.add(iv);
    }


    /*****************************************************************/
    /***/
    private void init() {

        Resources r = getResources();

        mContentDescription = r.getString(R.string.icon_option_desc);
        mPxIconDimension = r.getDimensionPixelSize(R.dimen.image_icon_picker_dimens);
        mPxIconMargin = r.getDimensionPixelSize(R.dimen.image_icon_margin);
        mImageViewIdCounter = 0;
        mImageViews = new ArrayList<>(ARRAY_CAPACITY);
        mImageViews.clear();
        mSelectedImageView = 0;
    }

}
