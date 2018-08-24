package com.shakuro.firebaseproject.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.database.SnapshotParser
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.functions.FirebaseFunctions
import com.shakuro.firebaseproject.Constants
import com.shakuro.firebaseproject.R
import com.shakuro.firebaseproject.entity.PostItem
import com.shakuro.firebaseproject.lists.ChatAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : BaseActivity(), GoogleApiClient.OnConnectionFailedListener {
    companion object {
        const val ANONYMOUS = "anonymous"
        const val LAUNCH_APP_BY_NOTIFICATION = "launch_app_by_notification"
        const val NOTIFICATION_KEY = "notification_key"
    }

    private val TAG = "MainActivity"
    private var mUsername: String? = null
    private var mPhotoUrl: String? = null
    private lateinit var mGoogleApiClient: GoogleApiClient

    private lateinit var mFirebaseDatabaseReference: DatabaseReference
    private lateinit var mFirebaseAdapter: FirebaseRecyclerAdapter<PostItem, ChatAdapter.FirechatMsgViewHolder>
    private lateinit var mLinearLayoutManager: LinearLayoutManager

    // Firebase instance variables
    private lateinit var mFirebaseAuth: FirebaseAuth
    private var mFirebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initAuth()

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build()

        mLinearLayoutManager = LinearLayoutManager(this)
        mLinearLayoutManager.stackFromEnd = true
        messageRecyclerView.layoutManager = mLinearLayoutManager

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().reference
        // test()
        val parser = SnapshotParser<PostItem> { dataSnapshot ->
            val postItem = dataSnapshot.getValue(PostItem::class.java)
            if(postItem != null) {
                postItem.id = dataSnapshot.key
                postItem
            } else {
                PostItem(dataSnapshot.key)
            }
        }

        val options = FirebaseRecyclerOptions.Builder<PostItem>()
                .setQuery(mFirebaseDatabaseReference.child(Constants.POSTS_CHILD), parser)
                .build()

        mFirebaseAdapter = object : ChatAdapter(this, options, {
            startActivity(Intent(this,
                        PostDetailsActivity::class.java).apply {
                    putExtra(PostDetailsActivity.POST_OBJECT, it)
                })
        }) {
            override fun onDataChanged() {
                super.onDataChanged()
                hideProgressDialog()
            }
        }

        showProgressDialog()

        mFirebaseAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                val chatMessageCount = mFirebaseAdapter.getItemCount()
                val lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition()
                if (lastVisiblePosition == -1 || positionStart >= chatMessageCount - 1 && lastVisiblePosition == positionStart - 1) {
                    messageRecyclerView.scrollToPosition(positionStart)
                }
            }
        })

        messageRecyclerView.adapter = mFirebaseAdapter

        val launchAppByNotification = intent.getBooleanExtra(LAUNCH_APP_BY_NOTIFICATION, false)
        if(launchAppByNotification) {
            val key = intent.getStringExtra(NOTIFICATION_KEY)
            if(key != null) {
                sendToFirebase(key)
            }
        }
    }

    fun sendToFirebase(key: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if(user != null) {
            val mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference()
            mFirebaseDatabaseReference.child("FCM").child(key).child("users").child(user.uid)
                    .child("opened").setValue(true)
        }
    }

    private fun test() {
        mFirebaseDatabaseReference.child("members").child("2").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                val map : HashMap<String, String> = p0?.value as HashMap<String, String>
                val keys = map.keys
                val tokens : ArrayList<Any> = ArrayList<Any>()
                val collection = hashMapOf<Any, Any>()
                keys.forEach {
                    mFirebaseDatabaseReference.child("users").child(it).child("tokens").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError?) {

                        }

                        override fun onDataChange(p0: DataSnapshot?) {
                            val iter = (p0?.value as HashMap<Any, Any>).keys
                            tokens.addAll(iter)
                            collection.put(it, iter)
                            val zzz = collection.mapKeys {
                                it.value.equals("tokenA")
                            }
                            var result: Any? = null
                            collection.forEach {
                                val r = (it.value as Iterable<String>).filter {
                                    it.equals("tokenA")
                                }
                                if(r != null && r.size > 0) {
                                    result = it
                                }
                            }
                        }
                    })
                }

            }

        })
    }

    public override fun onPause() {
        mFirebaseAdapter.stopListening()
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
        mFirebaseAdapter.startListening()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sign_out_menu -> {
                mFirebaseAuth.signOut()
                Auth.GoogleSignInApi.signOut(mGoogleApiClient)
                mUsername = ANONYMOUS
                startActivity(Intent(this, SignInActivity::class.java))
                finish()
                return true
            }
            R.id.change_user_info -> {
                startActivity(Intent(this, UserActivity::class.java))
                return true
            }
            R.id.call_test_func -> {
                callTestFunc()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun callTestFunc() {
        FirebaseFunctions.getInstance().getHttpsCallable("getUsers")
                .call(FirebaseAuth.getInstance().currentUser?.uid)
                .addOnFailureListener {
                    val a = 10
                    val b = a
                }
                .addOnSuccessListener {
                    val a = 10
                    val b = a
                }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult)
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show()
    }

    private fun initAuth() {
        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseUser = mFirebaseAuth.currentUser
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        } else {
            mUsername = mFirebaseUser?.displayName
            if (mFirebaseUser?.photoUrl != null) {
                mPhotoUrl = mFirebaseUser?.photoUrl.toString()
            }
        }
    }
}
