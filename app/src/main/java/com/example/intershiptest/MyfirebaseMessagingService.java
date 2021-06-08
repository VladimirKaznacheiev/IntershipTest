package com.example.intershiptest;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;

import io.realm.Realm;

public class MyfirebaseMessagingService extends FirebaseMessagingService {

    Realm realm;
    public static final String INTENT_FILTER = "INTENT_FILTER";
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.d("FCM", s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        realm = Realm.getDefaultInstance();

        if(remoteMessage.getNotification() != null){
            String messageTitle = remoteMessage.getNotification().getTitle();
            String messageBody = remoteMessage.getNotification().getBody();
            UserModel userModel = realm.where(UserModel.class).equalTo("id",Integer.parseInt(messageTitle)).findFirst();
            realm.beginTransaction();
            userModel.setChangesCount(Integer.parseInt(messageBody));
            realm.commitTransaction();
        }
    }
}
