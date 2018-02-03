package com.arielg.nowtify;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements
        IconPickerDialog.IconPickerDialogListener,
        View.OnLongClickListener {

    private ImageView mImageIcon = null;
    private EditText mEditTitle = null;
    private EditText mEditContent = null;
    private FrameLayout mButtonDebug = null;

    private int mCurrentIconIndex = 0;
    private Bitmap mCurrentIconBitmap = null;

    private Integer mEditedNowtificationId;
    private boolean mResumingFromCamera = false;

    //====================================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get UX members
        mImageIcon = findViewById(R.id.imageIcon);
        mEditTitle =  findViewById(R.id.editTitle);
        mEditContent = findViewById(R.id.editContent);
        mButtonDebug = findViewById(R.id.buttonDebug);

        /*
        mEditContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                Linkify.addLinks(mEditContent, Linkify.WEB_URLS|Linkify.EMAIL_ADDRESSES|Linkify.PHONE_NUMBERS);
            }
        });
        */

        mButtonDebug.setOnLongClickListener(this);
    }

    //====================================================================================================
    @Override
    protected void onResume() {
        super.onResume();

        // in the process of taking a photo (changing to camera) the application is paused and
        // resumed (onPause, onResume). The intent is also resumed. So if the original action was
        // ACTION_EDIT then handleIntents() will overwrite the photo bitmap
        if(mResumingFromCamera)
            mResumingFromCamera = false;
        else
            handleIntents();

        setNotifyIcon(mCurrentIconBitmap, mCurrentIconIndex);
    }

    //====================================================================================================
    public void onChangeNowtifyIcon(View view) {
        IconPickerDialog iconPickerDialog = new IconPickerDialog();
        iconPickerDialog.show(getFragmentManager(), mCurrentIconIndex);
    }

    //====================================================================================================
    public void onCreateNowtification(View view) {

        if(mEditTitle.getText().toString().isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.title_whoops_sorry)
                    .setMessage(R.string.error_empty_title)
                    .show();
            return;
        }

        ActiveNowtifications activeNowtifications = new ActiveNowtifications(this);
        activeNowtifications.initialize();

        if(activeNowtifications.size() >= ActiveNowtifications.MAX_ACTIVE_NOWTIFICATIONS) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.title_whoops_sorry)
                    .setMessage(getString(R.string.fmt_error_too_many_nowtifications, ActiveNowtifications.MAX_ACTIVE_NOWTIFICATIONS))
                    .show();
            return;
        }

        Nowtification n = new Nowtification(mCurrentIconIndex,
                mCurrentIconBitmap,
                mEditTitle.getText().toString(),
                mEditContent.getText().toString());

        // if in edit mode AND the nowtification exists in the list then update its values and
        // otherwise consider as a new nowtification
        if(mEditedNowtificationId != null && activeNowtifications.exists(mEditedNowtificationId) ) {
            n.setId(mEditedNowtificationId);
            activeNowtifications.get(mEditedNowtificationId).cloneData(n);
        } else {
            activeNowtifications.append(n.getId(), n);
        }

        activeNowtifications.commit();
        NowtificationService.Notify(this, n);
        finish();
    }
    
    //====================================================================================================
    // onDebug
    @Override
    public boolean onLongClick(View view) {

        final int MENU_ENTRY_LIST_CONTACTS = 0;
        final int MENU_ENTRY_LIST_NOWTIFYS = 1;
        final int MENU_ENTRY_CLEAR_NOWTIFYS = 2;

        DialogInterface.OnClickListener dlgDebugMenuListener = new DialogInterface.OnClickListener() {

            private final ActiveNowtifications nowtifys = new ActiveNowtifications(MainActivity.this);

            @Override
            public void onClick(DialogInterface dialogInterface, int which) {

                StringBuilder   list = new StringBuilder();
                String          fmt = getString(R.string.fmt_debug_list_nowtification_line);
                AlertDialog     dlg;

                switch(which) {

                    case MENU_ENTRY_LIST_CONTACTS:
                        ContactIcons contacts = new ContactIcons(MainActivity.this);

                        for(int i=0; i<contacts.size(); i++ ) {
                            list.append(String.format(fmt, i, contacts.get(i).id, contacts.get(i).displayNamePrimary));
                        }

                        dlg = new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Contacts")
                                .setMessage( list.length() == 0 ? "<empty>" : list.toString())
                                .show();

                        ((TextView)dlg.findViewById(android.R.id.message)).setTextSize(12);
                        break;

                    case MENU_ENTRY_LIST_NOWTIFYS:
                        nowtifys.initialize();

                        for (int i = 0; i < nowtifys.size(); i++) {
                            list.append(String.format(fmt, i, nowtifys.valueAt(i).getId(), nowtifys.valueAt(i).getTitle()));
                        }

                        dlg = new AlertDialog.Builder(MainActivity.this)
                                .setTitle("activeNowtifications")
                                .setMessage( list.length() == 0 ? "<empty>" : list.toString())
                                .show();

                        ((TextView)dlg.findViewById(android.R.id.message)).setTextSize(12);
                        break;

                    case MENU_ENTRY_CLEAR_NOWTIFYS:
                        nowtifys.initialize();
                        nowtifys.clear();
                        nowtifys.commit();
                        break;
                }
            }
        };

        new AlertDialog.Builder(this)
                .setTitle("Debug options")
                .setItems(R.array.menu_entries_debug, dlgDebugMenuListener).show();

        // return true so that it will not continue to the regular ItemClick
        return true;
    }

    //====================================================================================================
    @Override
    public void onFinishIconPickerDialog(Bitmap bitmap, int iconIndex, boolean isPhotoCamera) {

        mResumingFromCamera = isPhotoCamera;

        setNotifyIcon(bitmap, iconIndex);
    }
    
    //====================================================================================================
    private void handleIntents() {
        
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        
        if (action == null)
            return;

        mEditedNowtificationId = null;

        if(action.equals(NowtificationService.ACTION_EDIT)) {
            handleIntentActionEdit(intent.getIntExtra(NowtificationService.PARAM_NOTIFICATION_ID, -1));
        } else if(action.equals(Intent.ACTION_SEND) && type!=null && type.equals("text/plain")) {
            handleIntentActionSend(intent.getExtras().getString(Intent.EXTRA_TEXT, "").trim());
        }
    }

    //====================================================================================================
    private void handleIntentActionEdit(int id) {

        if (id == -1)
            return;

        ActiveNowtifications activeNowtifications = new ActiveNowtifications(this);
        activeNowtifications.initialize();
        Nowtification n = activeNowtifications.get(id);

        // if nowtification is missing from the saved list
        if(n == null) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.title_whoops_sorry)
                    .setMessage(getString(R.string.fmt_error_cant_edit_nowtify, id))
                    .show();
            return;
        }

        mCurrentIconIndex = n.getIconIndex();
        mCurrentIconBitmap = n.getIconBitmap();
        mEditTitle.setText(n.getTitle());
        mEditContent.setText(n.getContent());

        mEditedNowtificationId = n.getId();
    }

    //====================================================================================================
    private void handleIntentActionSend(String data) {

        String title;
        String content;
        int maxTitleLength = getResources().getInteger(R.integer.max_title_length);

        if( (data.length() <= maxTitleLength) && (!data.contains("\n")) ) {
            title = data;
            content = "";
        } else {

            title = data.substring(0, maxTitleLength);

            // find first newline in possible title
            int splitIndex = title.indexOf('\n');
            if(splitIndex > -1)
                title = title.substring(0, splitIndex);
            else {
                // title has no newline so find last word separator

                splitIndex = Math.max(Math.max(Math.max(Math.max(title.lastIndexOf(' '),
                        title.lastIndexOf('\t')),
                        title.lastIndexOf(',')),
                        title.lastIndexOf('.')),
                        title.lastIndexOf('-'));

                if(splitIndex > -1)
                    title = title.substring(0, splitIndex);
                else
                    title = "";	// The zero length will cause the entire data to be copied to content
            }
            content = data.substring(title.length()).trim();
        }

        mCurrentIconIndex = 0;
        mCurrentIconBitmap = null;
        mEditTitle.setText(title);
        mEditContent.setText(content);
    }
    
    //====================================================================================================
    private void setNotifyIcon(Bitmap bitmap, int iconIndex) {

        // use default icon
        if (bitmap == null) {
            mCurrentIconIndex = 0;
            mCurrentIconBitmap = Utils.getStockIconBitmapAt(getResources(), mCurrentIconIndex);
        } else {
            mCurrentIconIndex = iconIndex;
            mCurrentIconBitmap = bitmap;
        }

        mImageIcon.setImageBitmap(mCurrentIconBitmap);
        mImageIcon.setBackgroundResource(IconPicker.BK_COLOR_ICON_DEFAULT);

        // use ScaleType.CENTER only for stock icons (when iconIndex is invalid)
        mImageIcon.setScaleType( iconIndex==-1 ? ImageView.ScaleType.CENTER_CROP : ImageView.ScaleType.CENTER);
    }

}
