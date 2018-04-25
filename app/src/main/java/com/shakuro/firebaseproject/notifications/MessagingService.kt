package com.shakuro.firebaseproject.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MessagingService : FirebaseMessagingService() {
    private val TAG = "MyFMService"

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        // Handle data payload of FCM messages.
        Log.d(TAG, "FCM Message Id: " + remoteMessage!!.messageId!!)
        Log.d(TAG, "FCM Notification Message: " + remoteMessage.notification!!)
        Log.d(TAG, "FCM Data Message: " + remoteMessage.data)
    }
}