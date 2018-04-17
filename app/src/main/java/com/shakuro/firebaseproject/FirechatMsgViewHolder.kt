package com.shakuro.firebaseproject

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView

class FirechatMsgViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    var postTitleTextView: TextView
    var postDescriptionTextView: TextView
    var postImageView: ImageView

    init {
        postTitleTextView = itemView.findViewById(R.id.postTitleTextView) as TextView
        postDescriptionTextView = itemView.findViewById(R.id.postDescriptionTextView) as TextView
        postImageView = itemView.findViewById(R.id.postImageView) as ImageView
    }
}