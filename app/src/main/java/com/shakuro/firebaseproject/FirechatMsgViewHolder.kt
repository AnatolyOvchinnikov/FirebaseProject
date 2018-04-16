package com.shakuro.firebaseproject

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView

class FirechatMsgViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    var msgTextView: TextView
    var userTextView: TextView
    var userImageView: ImageView

    init {
        msgTextView = itemView.findViewById(R.id.messageTextView) as TextView
        userTextView = itemView.findViewById(R.id.userTextView) as TextView
        userImageView = itemView.findViewById(R.id.userImageView) as ImageView
    }
}