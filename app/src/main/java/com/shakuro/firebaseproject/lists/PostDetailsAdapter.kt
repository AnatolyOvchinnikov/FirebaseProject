package com.shakuro.firebaseproject.lists

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.storage.FirebaseStorage
import com.shakuro.firebaseproject.R
import com.shakuro.firebaseproject.entity.CommentItem
import com.shakuro.firebaseproject.extentions.timestampToDate
import kotlinx.android.synthetic.main.comment_item_layout.view.*


class PostDetailsAdapter(val context: Context, options: FirebaseRecyclerOptions<Any>) : FirebaseRecyclerAdapter<Any, RecyclerView.ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.comment_item_layout, parent, false)

        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: Any) {
        if(holder is CommentViewHolder) {
            holder.bind(model as CommentItem)
        }
    }

    inner class CommentViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        fun bind(commentItem: CommentItem) {
            itemView.apply {
                this.userTextView.text = commentItem.userName
                this.messageTextView.text = commentItem.message
                this.messageTimestamp.text = (commentItem.timestamp as Long).timestampToDate()
                loadAvatar(commentItem.userId)
            }
        }

        private fun loadAvatar(userId: String?) {
            val storageRef = FirebaseStorage.getInstance().reference
            val pathReference = storageRef.child("users/${userId}/avatar.jpg")
            Glide.with(context)
                    .using(FirebaseImageLoader())
                    .load(pathReference)
                    .placeholder(R.mipmap.ic_launcher)
                    .into(itemView.userImageView)
        }
    }
}