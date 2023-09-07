package com.subhdroid.rpbs.CloudMessage;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;
import com.subhdroid.rpbs.R;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String channel_id = "Service remainder channel";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            r.setLooping(false);
        }

        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {100, 300, 300, 300};
        v.vibrate(pattern, -1);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,
                channel_id);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            notificationBuilder.setSmallIcon(R.mipmap.rpbs)
                    .setContentText(remoteMessage.getNotification().getBody())
                    .setContentTitle(remoteMessage.getNotification().getTitle())
                    .setAutoCancel(false)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getNotification().getBody()))
                    .setChannelId(channel_id)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build();

            nm.createNotificationChannel(new NotificationChannel(channel_id,"Slot Booking",
                    NotificationManager.IMPORTANCE_HIGH));
        }else {
            notificationBuilder.setSmallIcon(R.mipmap.rpbs)
                    .setContentText(remoteMessage.getNotification().getBody())
                    .setContentTitle(remoteMessage.getNotification().getTitle())
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getNotification().getBody()))
                    .setAutoCancel(false)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build();
        }

        nm.notify(1,notificationBuilder.build());

    }
}