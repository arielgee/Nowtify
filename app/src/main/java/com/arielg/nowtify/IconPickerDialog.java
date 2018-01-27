package com.arielg.nowtify;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.VectorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;


public class IconPickerDialog extends DialogFragment implements IconPicker.IconPickerListener,
        ContactIconPickerDialog.ContactIconPickerDialogListener,
        View.OnClickListener {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_SELECT = 2;

    private Integer mSelectedIconIndex = 0;

    private ScrollView mScrollView = null;
    private IconPicker mIconPicker = null;


    public interface IconPickerDialogListener {
        void onFinishIconPickerDialog(Bitmap bitmap, int iconIndex, boolean isCamera);
    }

    private final View.OnLayoutChangeListener mLayoutChangeListener = new View.OnLayoutChangeListener() {
        public void onLayoutChange(View v, int l, int t, int r, int b, int ol, int ot, int or, int ob) {
            scrollToSelected();
        }
    };

    //===================================================================
    @SuppressLint("InflateParams") @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View viewRoot = inflater.inflate(R.layout.icon_picker_dialog, null);
        builder.setView(viewRoot);
        builder.setTitle(R.string.pick_icon_dialog_title);

        LinearLayout mLayoutCamera = (LinearLayout) (viewRoot.findViewById(R.id.layout_camera));
        LinearLayout mLayoutGallery = (LinearLayout) (viewRoot.findViewById(R.id.layout_gallery));
        LinearLayout mLayoutContacts = (LinearLayout) (viewRoot.findViewById(R.id.layout_contacts));
        mScrollView = (ScrollView)(viewRoot.findViewById(R.id.scroll_view));
        mIconPicker = (IconPicker)(viewRoot.findViewById(R.id.layout_icons));

        mLayoutCamera.setOnClickListener(this);
        mLayoutGallery.setOnClickListener(this);
        mLayoutContacts.setOnClickListener(this);
        mIconPicker.setIconSelectedListener(this);
        mScrollView.addOnLayoutChangeListener(mLayoutChangeListener);

        createIconPicker();
        Utils.verifyReadContactsPermissions(getActivity());

        // Create the AlertDialog object and return it
        return builder.create();
    }

    //===================================================================
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_camera:
                onCamera();
                break;
            case R.id.layout_gallery:
                onGallery();
                break;
            case R.id.layout_contacts:
                onContacts();
                break;
            default:
                break;
        }

    }

    //===================================================================
    private void onCamera() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    //===================================================================
    private void onGallery(){

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_IMAGE_SELECT);
        }
    }

    //===================================================================
    private void onContacts(){
        ContactIconPickerDialog iconPickerDialog = new ContactIconPickerDialog();
        iconPickerDialog.show(getFragmentManager(), this);
    }

    //===================================================================
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode != Activity.RESULT_OK)
            return;

        Bitmap bitmap = null;

        if(requestCode == REQUEST_IMAGE_CAPTURE) {

            bitmap = data.getParcelableExtra("data");

        } else if(requestCode == REQUEST_IMAGE_SELECT) {

            Uri imageUri = data.getData();
            try {
                bitmap = Utils.getThumbnail(getActivity(), imageUri);
            } catch (Exception e) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.title_whoops_sorry)
                        .setMessage(getString(R.string.error_get_thumbnail) + "\n" + e.toString())
                        .show();
            }
        }
        dialogOk(bitmap);
    }

    //===================================================================
    @Override
    public void onFinishContactIconPickerDialog(Bitmap bitmap) {
        dialogOk(bitmap);
    }


    //===================================================================
    @Override
    public void onIconSelectedListener() {
        dialogOk(null);
    }

    //===================================================================
    private void createIconPicker() {

        Resources r = getResources();

        mIconPicker.setRowCount(r.getInteger(R.integer.max_icon_grid_row_count));
        mIconPicker.setColumnCount(calculateGridColumnCount(r));

        TypedArray icons = r.obtainTypedArray(R.array.nowtify_stock_icons);

        for(int i=0; i<icons.length(); i++ ) {
            mIconPicker.setIcon(Utils.getBitmapFromVectorDrawable((VectorDrawable)r.getDrawable(icons.getResourceId(i, -1), null)));
        }

        icons.recycle();

        if((mSelectedIconIndex < mIconPicker.getSize()) && (mSelectedIconIndex > -1))
            mIconPicker.setSelectedIndex(mSelectedIconIndex);
    }

    //===================================================================
    public void show(FragmentManager manager, Integer selectedIcon) {

        mSelectedIconIndex = selectedIcon;
        super.show(manager, "IconPickerDialog");
    }

    //===================================================================
    private void dialogOk(Bitmap bitmap) {

        IconPickerDialogListener listener = (IconPickerDialogListener)getActivity();

        // if the bitmap is NULL then its not from the camera
        if(bitmap == null)
            listener.onFinishIconPickerDialog(mIconPicker.getIcon(), mIconPicker.getSelectedIndex(), false);
        else
            listener.onFinishIconPickerDialog(bitmap, -1, true);

        dismiss();
    }


    //===================================================================
    private int calculateGridColumnCount(Resources r) {

        Display display = getActivity().getWindow().getWindowManager().getDefaultDisplay();

        Point point = new Point();
        display.getSize(point);

        // add the imageView margins and the scrollView margins
        int iconDimension = r.getDimensionPixelSize(R.dimen.image_icon_picker_dimens)
                + (r.getDimensionPixelSize(R.dimen.image_icon_margin)*4);

        // number of icons that can fit the width of the display minus 1
        return point.x/iconDimension-1;
    }

    //===================================================================
    private void scrollToSelected() {

        ImageView selected = mIconPicker.getSelectedIcon();
        if(selected != null)
            mScrollView.scrollTo(0, selected.getTop());
    }
}
