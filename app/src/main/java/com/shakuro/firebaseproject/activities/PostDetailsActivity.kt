package com.shakuro.firebaseproject.activities

import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.shakuro.firebaseproject.Constants.Companion.COMMENTS_CHILD
import com.shakuro.firebaseproject.R
import com.shakuro.firebaseproject.entity.CommentItem
import kotlinx.android.synthetic.main.post_details_layout.*

class PostDetailsActivity : BaseActivity() {

    companion object {
        const val POST_ID = "post id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_details_layout)

        val a = ServerValue.TIMESTAMP

        val postId = intent.getStringExtra(POST_ID)

        commentSendBtn.setOnClickListener { sendComment(postId) }
    }

    private fun sendComment(postId: String) {
        val mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference()

//        mFirebaseDatabaseReference.addValueEventListener(object : ValueEventListener{
//            override fun onCancelled(p0: DatabaseError?) {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//            }
//
//            override fun onDataChange(p0: DataSnapshot?) {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//            }
//
//        })

//        mFirebaseDatabaseReference.setValue(ServerValue.TIMESTAMP)


        FirebaseAuth.getInstance().currentUser?.let {
            val comment = CommentItem(
                    userId = it.uid,
                    message = commentFieldView.getText().toString(),
                    userName = it.email,
                    timestamp = 1L )

            mFirebaseDatabaseReference.child(COMMENTS_CHILD).child(postId)
                    .push().setValue(comment)

            commentFieldView.setText("")
        }
    }
}