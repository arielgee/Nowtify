package com.arielg.nowtify;

import android.content.Context;
import android.util.SparseArray;

class ActiveNowtifications {

    public static final int MAX_ACTIVE_NOWTIFICATIONS = 10;

    private final Context mContext;

    public class NowtificationsArray extends SparseArray<Nowtification> { }

    private final NowtificationsArray mNowtificationsArray = new NowtificationsArray();

    public ActiveNowtifications(Context context) {
        mContext = context;
    }

    public boolean initialize() {
        ActiveNowtificationsFile f = new ActiveNowtificationsFile(mContext);
        return f.read(mNowtificationsArray);
    }

    public boolean commit() {
        return this.save();
    }

    private boolean save() {
        ActiveNowtificationsFile f = new ActiveNowtificationsFile(mContext);
        return f.write(mNowtificationsArray);
    }

    public boolean exists(int id) {
        return mNowtificationsArray.indexOfKey(id) >= 0;
    }

    public int size() {
        return mNowtificationsArray.size();
    }

    public void append(int id, Nowtification n) {
        mNowtificationsArray.append(id, n);
    }

    public Nowtification get(int id) {
        return mNowtificationsArray.get(id);
    }

    public void remove(int id) {
        mNowtificationsArray.remove(id);
    }

    public void clear() {
        mNowtificationsArray.clear();
    }

    public int keyAt(int index) {
        return mNowtificationsArray.keyAt(index);
    }

    public Nowtification valueAt(int index) {
        return mNowtificationsArray.valueAt(index);
    }


}
