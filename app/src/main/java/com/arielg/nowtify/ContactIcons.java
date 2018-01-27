package com.arielg.nowtify;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;

import java.io.InputStream;
import java.util.ArrayList;

class ContactIcons extends ArrayList<ContactIcons.Contact> {

    //===================================================================
    public class Contact {

        public final long id;
        public final String displayNamePrimary;
        public final Bitmap bitmap;

        Contact(long id, String displayNamePrimary, Bitmap bitmap) {
            this.id = id;
            this.displayNamePrimary = displayNamePrimary;
            this.bitmap = bitmap;
        }
    }

    //===================================================================
    public ContactIcons(Context context) {

        ContentResolver resolver = context.getContentResolver();

        String PROJECTION[] ={ContactsContract.Contacts._ID,
                ContactsContract.Contacts.LOOKUP_KEY,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY};

        String SELECTION = ContactsContract.Contacts.PHOTO_THUMBNAIL_URI + " NOT NULL";
        //String SORT = ContactsContract.Contacts.DISPLAY_NAME + " ASC";

        Cursor cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, PROJECTION, SELECTION, null, null);

        if(cursor != null) {
            if(cursor.getCount() > 0) {

                long id;
                String name;
                InputStream inputStream;

                while (cursor.moveToNext()) {

                    id = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));

                    /*
                    if(false) {
                        // delete a contact
                        String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                        Log.d("[###]", "ContactIcons: id=" + id);
                        Log.d("[###]", "ContactIcons: uri=" + uri.toString());
                        Log.d("[###]", "ContactIcons: name=" + name);
                        //int i = resolver.delete(uri, null, null);
                        //Log.d("[###]", "ContactIcons: deleted=" + i);
                    }
                    */

                    inputStream = ContactsContract.Contacts.openContactPhotoInputStream(resolver,
                            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id));

                    if(inputStream != null)
                        this.add(new Contact(id, name, BitmapFactory.decodeStream(inputStream)));
                }
            }
            cursor.close();
        }
    }
}
