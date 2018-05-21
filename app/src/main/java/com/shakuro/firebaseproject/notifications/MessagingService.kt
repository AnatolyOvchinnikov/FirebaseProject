package com.shakuro.firebaseproject.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.shakuro.firebaseproject.R
import com.shakuro.firebaseproject.activities.MainActivity
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicInteger






class MessagingService : FirebaseMessagingService() {
    private val TAG = "MyFMService"

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        // Handle data payload of FCM messages.
        if (remoteMessage != null && remoteMessage.getData().size > 0) {
            val data = remoteMessage.data.get("firebase_data")
            sendToFirebase()
            var title: String? = null
            var body: String? = null
            try {
                val json = JSONObject(data)
                if(json.has("title") && !json.isNull("title")) {
                    title = json.optString("title");
                }
                if(json.has("body") && !json.isNull("body")) {
                    body = json.optString("body");
                }
            } catch (e: JSONException) {

            }

            if(title != null && body != null) {
                sendNotification(title, body)
            }
        }
    }

    fun sendToFirebase() {
        val user = FirebaseAuth.getInstance().currentUser
        if(user != null) {
            val mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference()
            mFirebaseDatabaseReference.child("FCM")
                    .push().child("received").setValue(true)
        }
    }

    fun sendNotification(title: String, body: String) {
        val context = baseContext
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.action = java.lang.Long.toString(System.currentTimeMillis())
        val pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(NotificationID.id, notificationBuilder.build())
    }

    object NotificationID {
        private val c = AtomicInteger(0)
        val id: Int
            get() = c.incrementAndGet()
    }
}