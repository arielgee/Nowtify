package com.arielg.nowtify;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

class ActiveNowtificationsFile {

    private static final String FILE_NAME = "di2nd83jd8h5kod0Qsd7";

    private File mFile = null;

    public ActiveNowtificationsFile(Context context) {
        mFile = new File(context.getCacheDir(), FILE_NAME);
    }

    public boolean read(ActiveNowtifications.NowtificationsArray nowtificationsArray) {

        long length = mFile.length();

        // empty file - empty string
        if(length == 0)
            return false;

        try {
            FileInputStream mFis = new FileInputStream(mFile);
            ObjectInputStream objInput = new ObjectInputStream(mFis);

            int size = objInput.readInt();
            for(int i=0; i<size; i++) {
                Nowtification n = Nowtification.read(objInput);
                nowtificationsArray.append(n.getId(), n);
            }

            objInput.close();
            mFis.close();
            return true;

        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean write(ActiveNowtifications.NowtificationsArray nowtificationsArray) {

        int size = nowtificationsArray.size();

        if(size == 0) {
            mFile.delete();
            return true;
        }

        try {
            FileOutputStream mFos = new FileOutputStream(mFile);
            ObjectOutputStream objOutput = new ObjectOutputStream(mFos);

            objOutput.writeInt(size);
            for(int i=0; i<size; i++)
                nowtificationsArray.valueAt(i).write(objOutput);

            objOutput.close();
            mFos.close();
            return true;

        } catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
