package com.cobresun.brun.pantsorshorts;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;


public class NotificationReceiver extends BroadcastReceiver {

    public static int REQUEST_CODE;

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent launchIntent = new Intent(context, MainActivity.class);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, REQUEST_CODE, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String CHANNEL_ID = "PoS";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Morning Notification";
            String Description = "Daily morning notification to check Pants Or Shorts";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            notificationManager.createNotificationChannel(mChannel);
        }

        // TODO: Make this temperature fetch from a util that is shared with MainActivityPresenter
        String temperature = "8";
//        String notifText = "Temperature today is: "+ temperature + "\u00B0" + "C";
        String notifText = "Check the app for today's recommendation!";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.mipmap.ic_launcher_foreground))
                .setContentTitle("PantsOrShorts")
                .setContentText(notifText)
                .setAutoCancel(true);

        assert notificationManager != null;
        notificationManager.notify(REQUEST_CODE, builder.build());

    }
}
