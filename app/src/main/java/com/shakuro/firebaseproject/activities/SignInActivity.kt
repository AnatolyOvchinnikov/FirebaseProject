package com.shakuro.firebaseproject.activities

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.iid.FirebaseInstanceId
import com.shakuro.firebaseproject.R
import kotlinx.android.synthetic.main.activity_sign_in.*
import java.io.IOException


class SignInActivity : BaseActivity(), GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    private val TAG = "SignInActivity"
    private val RC_SIGN_IN = 9001

    private lateinit var mGoogleApiClient: GoogleApiClient

    // Firebase instance variables
    private lateinit var mFirebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Set click listeners
        signInButton.setOnClickListener(this)
        registerBtn.setOnClickListener(this)
        loginBtn.setOnClickListener(this)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        // Initialize FirebaseAuth
        mFirebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onClick(v: View?) {
        when (v?.getId()) {
            R.id.signInButton -> signIn()
            R.id.registerBtn -> createUser(loginEditText.text.toString(), passwordEditText.text.toString())
            R.id.loginBtn -> signIn(loginEditText.text.toString(), passwordEditText.text.toString())
        }
    }

    private fun signIn() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                // Google Sign-In was successful, authenticate with Firebase
                val account = result.signInAccount
                firebaseAuthWithGoogle(account)
            } else {
                // Google Sign-In failed
                Log.e(TAG, "Google Sign-In failed.")
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?) {
        Log.d(TAG, "firebaseAuthWithGooogle:" + acct!!.id!!)
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful)

                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful) {
                        Log.w(TAG, "signInWithCredential", task.exception)
                        Toast.makeText(this@SignInActivity, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                    } else {
                        authSuccess()
                    }
                }
    }

    private fun createUser(email: String, password: String) {
        if(isValidEmail(email)) {
            mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, OnCompleteListener<AuthResult> { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success")
                            val user = mFirebaseAuth.getCurrentUser()
                            authSuccess()
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.exception)
                            Toast.makeText(this@SignInActivity, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show()
                        }
                    })
        } else {
            Toast.makeText(this@SignInActivity, "Wrong email",
                    Toast.LENGTH_SHORT).show()
        }
    }

    private fun signIn(email: String, password: String) {
        if(isValidEmail(email)) {
            mFirebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, OnCompleteListener<AuthResult> { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success")
                            val user = mFirebaseAuth.getCurrentUser()
                            authSuccess()
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                            Toast.makeText(this@SignInActivity, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show()
                        }
                    })
        } else {
            Toast.makeText(this@SignInActivity, "Wrong email",
                    Toast.LENGTH_SHORT).show()
        }
    }

    private fun authSuccess() {
        AsyncTask.execute {
            try {
                // Resets Instance ID and revokes all tokens.
                FirebaseInstanceId.getInstance().deleteInstanceId()

                // Now manually call onTokenRefresh()
                Log.d(TAG, "Getting new token")
                FirebaseInstanceId.getInstance().token
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        startActivity(Intent(this@SignInActivity, MainActivity::class.java))
        finish()
    }

    private fun isValidEmail(target: CharSequence?): Boolean {
        return if (target == null) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(target).matches()
        }
    }
}