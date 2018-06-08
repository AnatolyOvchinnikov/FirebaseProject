package com.shakuro.firebaseproject.notifications

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

class InstanceIdService : FirebaseInstanceIdService() {
    private val TAG = "MyFirebaseIIDService"
    private val FRIENDLY_ENGAGE_TOPIC = "friendly_engage"

    /**
     * The Application's current Instance ID token is no longer valid and thus a new one must be requested.
     */
    override fun onTokenRefresh() {
        // If you need to handle the generation of a token, initially or
        // after a refresh this is where you should do that.
        val token = FirebaseInstanceId.getInstance().token
        Log.d(TAG, "FCM Token: " + token!!)
        sendRegistrationToServer(token)

        // Once a token is generated, we subscribe to topic.
//        FirebaseMessaging.getInstance()
//                .subscribeToTopic(FRIENDLY_ENGAGE_TOPIC)
    }

    private fun sendRegistrationToServer(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        FirebaseDatabase.getInstance().reference.child("users/${userId}").child("fcm_token").setValue(token)
    }
}