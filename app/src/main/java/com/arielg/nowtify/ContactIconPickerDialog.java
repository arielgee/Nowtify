package com.arielg.nowtify;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;

public class ContactIconPickerDialog extends DialogFragment implements IconPicker.IconPickerListener {

    private ScrollView mScrollView = null;
    private IconPicker mIconPicker = null;

    private ContactIconPickerDialogListener mListenerFragment;

    public interface ContactIconPickerDialogListener {
        void onFinishContactIconPickerDialog(Bitmap bitmap);
    }

    private final View.OnLayoutChangeListener mLayoutChangeListener = new View.OnLayoutChangeListener() {
        public void onLayoutChange(View v, int l, int t, int r, int b, int ol, int ot, int or, int ob) {
            scrollToSelected();
        }
    };

    //===================================================================
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View viewRoot = inflater.inflate(R.layout.contact_icon_picker_dialog, null);
        builder.setView(viewRoot);
        builder.setTitle(R.string.pick_contact_icon_dialog_title);

        mScrollView = viewRoot.findViewById(R.id.scroll_view);
        mIconPicker = viewRoot.findViewById(R.id.layout_icons);

        mIconPicker.setIconSelectedListener(this);
        mScrollView.addOnLayoutChangeListener(mLayoutChangeListener);

        createContactIconPicker();

        // Create the AlertDialog object and return it
        return builder.create();
    }

    //===================================================================
    @Override
    public void onIconSelectedListener() {
        dialogOk();
    }

    //===================================================================

    private void createContactIconPicker() {

        Resources r = getResources();

        mIconPicker.setRowCount(r.getInteger(R.integer.max_icon_grid_row_count));
        mIconPicker.setColumnCount(calculateGridColumnCount(r));

        mIconPicker.setIconScaleTypeCenterCrop(true);

        ContactIcons contacts = new ContactIcons(getActivity());

        for(int i=0; i<contacts.size(); i++ ) {
            mIconPicker.setIcon(contacts.get(i).bitmap);
        }
    }

    //===================================================================
    public void show(FragmentManager manager, ContactIconPickerDialogListener listenerFragment) {

        mListenerFragment = listenerFragment;
        super.show(manager, "ContactIconPickerDialog");
    }

    //===================================================================
    private void dialogOk() {

        ContactIconPickerDialogListener listener = mListenerFragment;
        listener.onFinishContactIconPickerDialog(mIconPicker.getIcon());
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
