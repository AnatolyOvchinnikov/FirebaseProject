package com.shakuro.firebaseproject.activities

import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.database.SnapshotParser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.shakuro.firebaseproject.Constants
import com.shakuro.firebaseproject.Constants.Companion.COMMENTS_CHILD
import com.shakuro.firebaseproject.R
import com.shakuro.firebaseproject.entity.CommentItem
import com.shakuro.firebaseproject.entity.PostItem
import com.shakuro.firebaseproject.lists.PostDetailsAdapter
import kotlinx.android.synthetic.main.post_details_layout.*




class PostDetailsActivity : BaseActivity() {

    companion object {
        const val POST_OBJECT = "post object"
    }

    private lateinit var mFirebaseDatabaseReference: DatabaseReference
    private lateinit var mFirebaseAdapter: FirebaseRecyclerAdapter<Any, RecyclerView.ViewHolder>
    private lateinit var mLinearLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_details_layout)

        val postItem : PostItem = intent.getSerializableExtra(POST_OBJECT) as PostItem
        commentSendBtn.setOnClickListener {
            postItem.id?.let {
                hideKeyboard()
                val handler = Handler()
                handler.postDelayed({
                    sendComment(it)
                }, 100)
            }
        }

        initAdapter(postItem)
    }

    public override fun onPause() {
        mFirebaseAdapter.stopListening()
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
        mFirebaseAdapter.startListening()
    }

    private fun initAdapter(postItem : PostItem) {
        mLinearLayoutManager = LinearLayoutManager(this)
        mLinearLayoutManager.stackFromEnd = false
        commentsViewContainer.layoutManager = mLinearLayoutManager

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().reference
        val parser = SnapshotParser<Any> { dataSnapshot ->
            val postItem = dataSnapshot.getValue(CommentItem::class.java)
            if(postItem != null) {
                postItem.id = dataSnapshot.key
                postItem
            } else {
                CommentItem(dataSnapshot.key)
            }
        }

        val options = FirebaseRecyclerOptions.Builder<Any>()
                .setQuery(mFirebaseDatabaseReference.child(Constants.COMMENTS_CHILD).child(postItem.id), parser)
                .build()

        mFirebaseAdapter = PostDetailsAdapter(this, options)

        mFirebaseAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                val chatMessageCount = mFirebaseAdapter.getItemCount()
                val lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition()
                if (lastVisiblePosition == -1 || positionStart >= chatMessageCount - 1 && lastVisiblePosition == positionStart - 1) {
                    commentsViewContainer.scrollToPosition(positionStart)
                }
            }
        })
        commentsViewContainer.adapter = mFirebaseAdapter
        commentsViewContainer.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        commentsViewContainer.setItemAnimator(DefaultItemAnimator())
    }

    private fun sendComment(postId: String) {
        val mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference()

        FirebaseAuth.getInstance().currentUser?.let {
            val comment = CommentItem(
                    userId = it.uid,
                    postId = postId,
                    message = commentFieldView.getText().toString(),
                    userName = it.email,
                    timestamp = ServerValue.TIMESTAMP)

            mFirebaseDatabaseReference.child(COMMENTS_CHILD).child(postId)
                    .push().setValue(comment)

            commentFieldView.setText("")
        }
    }
}