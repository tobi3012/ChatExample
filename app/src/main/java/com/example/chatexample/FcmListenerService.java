package com.example.chatexample;

import android.app.Notification;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.api.account.register.RegisterUserClientService;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.notification.MobiComPushReceiver;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class  FcmListenerService extends FirebaseMessagingService {

    private static final String TAG = "ApplozicGcmListener";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.i(TAG, "Message data:" + remoteMessage.getData());
//        Notification notification = new NotificationCompat.Builder(this)
//                .setContentTitle(remoteMessage.getNotification().getTitle())
//                .setContentText(remoteMessage.getNotification().getBody())
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .build();
//
//        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
//        manager.notify(123, notification);

        if (remoteMessage.getData().size() > 0) {
            if (Applozic.isApplozicNotification(this, remoteMessage.getData())) {
                Log.i(TAG, "Applozic notification processed");
                MobiComPushReceiver.processMessageAsync(this, remoteMessage.getData());
                return;
            }
        }

    }

    @Override
    public void onNewToken(String registrationId) {
        super.onNewToken(registrationId);

        Log.i(TAG, "Found Registration Id @:" + registrationId);
        if (MobiComUserPreference.getInstance(this).isRegistered()) {
            try {
                RegistrationResponse registrationResponse = new RegisterUserClientService(this).updatePushNotificationId(registrationId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
