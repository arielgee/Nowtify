package com.arielg.nowtify;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.Patterns;

import java.net.MalformedURLException;
import java.net.URL;

public class NowtificationService extends IntentService {

    private static final String TAG = "[###] NowtifySrv";

    private static final String ACTION_DISMISS = "com.arielg.nowtify.nowtificationService.action.DISMISS";
    public static final String ACTION_EDIT = "com.arielg.nowtify.nowtificationService.action.EDIT";
    public static final String PARAM_NOTIFICATION_ID = "com.arielg.nowtify.nowtificationService.extra.NOTIFICATION_ID";
    private static final String NOTIFICATION_CHANNEL_ID = "com.arielg.nowtify.channel";

    private final static NowtificationService mInstance = new NowtificationService();

    private static NotificationChannel mNotificationChannel = null;

    ////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    @SuppressWarnings("WeakerAccess")
    public NowtificationService() {
        super("NowtificationService");
    }

    ////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    public static void Notify(Context context, Nowtification n) {

        if(mNotificationChannel == null) {
            mInstance.createNotificationChannel(context);
        }

        mInstance.notify(context, n);
    }

    ////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent == null)
            return;

        final String action = intent.getAction();
        int paramNotificationID;

        if (action.equals(ACTION_DISMISS)) {
            paramNotificationID = intent.getIntExtra(PARAM_NOTIFICATION_ID, -1);
            handleActionDismiss(paramNotificationID);
        } else if (action.equals(ACTION_EDIT)) {
            paramNotificationID = intent.getIntExtra(PARAM_NOTIFICATION_ID, -1);
            handleActionEdit(paramNotificationID);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    private void createNotificationChannel(Context context) {

        NotificationManager notificationMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // The user-visible name of the channel + The user-visible description of the channel.
        CharSequence name = context.getString(R.string.channel_name);
        String description = context.getString(R.string.channel_description);

        mNotificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
        // Configure the notification channel.
        mNotificationChannel.setDescription(description);

        // Sets the notification light color for notifications posted to this
        // channel, if the device supports this feature.
        mNotificationChannel.enableLights(true);
        mNotificationChannel.setLightColor(Color.CYAN);
        mNotificationChannel.enableVibration(true);
        mNotificationChannel.setVibrationPattern(new long[]{100, 200, 300});
        notificationMgr.createNotificationChannel(mNotificationChannel);
    }

    ////////////////////////////////////////////////////////////////////////////////////
    // Handle action Dismiss in the provided background thread with the provided parameters.
    private void handleActionDismiss(int notificationID) {
    
        // invalid
        if (notificationID < 0)
            return;

        ActiveNowtifications activeNotifications = new ActiveNowtifications(getBaseContext());
        activeNotifications.initialize();
        activeNotifications.remove(notificationID);
        activeNotifications.commit();

        // Gets an instance of the NotificationManager service
        NotificationManager notifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notifyMgr.cancel(notificationID);
    }

    ////////////////////////////////////////////////////////////////////////////////////
    // Handle action Edit in the provided background thread with the provided parameters.
    private void handleActionEdit(int notificationID) {

        // invalid
        if (notificationID < 0)
            return;

        // send notification ID to editor
        // FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS: otherwise a launch from the recent menu
        // will remember the intent and its action and will reload the same notification
        // from the active notifications cache. Practice redoing the Edit action.
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.setAction(ACTION_EDIT);
        intent.putExtra(PARAM_NOTIFICATION_ID, notificationID);
        getApplication().startActivity(intent);

        // close notification drawer
        getBaseContext().sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }

    ////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    private void notify(Context context, Nowtification n) {

        NotificationCompat.Style style = null;
        PendingIntent contentIntent = null;

        if(n.getContent().contains("\n")) {
            style = new NotificationCompat.BigTextStyle().bigText(n.getContent());
        }

        String sUri = getFirstSupportedUriInContent(n.getContent());
        if(sUri != null) {

            Log.d(TAG, "URI: " + sUri);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sUri));

            contentIntent = PendingIntent.getActivity(context, n.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setTicker(n.getTitle())
                .setContentTitle(n.getTitle())
                .setContentText(n.getContent())
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setWhen(n.getWhen())
                .setOngoing(n.isOngoing())
                .setSmallIcon(R.drawable.ic_nowtify_icon00)
                .setLargeIcon(n.getIconBitmap())
                .setStyle(style)
                .setContentIntent(contentIntent);

        Intent dismissIntent = new Intent(context, NowtificationService.class);
        dismissIntent.setAction(ACTION_DISMISS);
        dismissIntent.putExtra(PARAM_NOTIFICATION_ID, n.getId());
        PendingIntent piDismiss = PendingIntent.getService(context, n.getId(), dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action actionDismiss;
        actionDismiss = new NotificationCompat.Action.Builder(android.R.drawable.ic_menu_close_clear_cancel, context.getString(R.string.action_dismiss), piDismiss).build();

        Intent editIntent = new Intent(context, NowtificationService.class);
        editIntent.setAction(ACTION_EDIT);
        editIntent.putExtra(PARAM_NOTIFICATION_ID, n.getId());
        PendingIntent piEdit = PendingIntent.getService(context, n.getId(), editIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action actionEdit;
        actionEdit = new NotificationCompat.Action.Builder(android.R.drawable.ic_menu_edit, context.getString(R.string.action_edit), piEdit).build();

        builder.addAction(actionDismiss).addAction(actionEdit);

        // Gets an instance of the NotificationManager service
        NotificationManager notifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        notifyMgr.notify(n.getId(), builder.build());
    }

    ////////////////////////////////////////////////////////////////////////////////////
    private String getFirstSupportedUriInContent(String content) {

        // URI's don't have spaces
        String[] parts = content.split("\\s+");

        for(String s : parts) {

            // the only exception is if it starts with an 'www.' (case-insensitive)
            if(s.matches("^(?i)www\\..*"))
                s = "http://" + s;

            try {
                new URL(s);
                return s;
            } catch (MalformedURLException e) {}

            if(Patterns.EMAIL_ADDRESS.matcher(s).matches())
                return "mailto:" + s;
        }

        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////
    // Restore all notifications from cache file
    public static void restoreActiveNotifications(Context context) {

        ActiveNowtifications ActiveNowtifications = new ActiveNowtifications(context);
        ActiveNowtifications.initialize();

        int size = ActiveNowtifications.size();
        NowtificationService service = new NowtificationService();

        for(int idx=0; idx<size; idx++)
            service.notify(context, ActiveNowtifications.valueAt(idx));
    }
}
