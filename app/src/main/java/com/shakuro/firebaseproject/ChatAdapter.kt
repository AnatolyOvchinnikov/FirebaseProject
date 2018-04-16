package com.shakuro.firebaseproject

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class ChatAdapter(val context: Context, options: FirebaseRecyclerOptions<FriendlyMessage>) : FirebaseRecyclerAdapter<FriendlyMessage, FirechatMsgViewHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FirechatMsgViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.dialog_item_layout, parent, false)

        return FirechatMsgViewHolder(view)
    }

    override fun onBindViewHolder(holder: FirechatMsgViewHolder, position: Int, model: FriendlyMessage) {
        holder.msgTextView.setText(model.text)
        holder.userTextView.setText(model.name)
        if (model.photoUrl == null) {
            holder.userImageView
                    .setImageDrawable(ContextCompat
                            .getDrawable(context,
                                    R.mipmap.ic_launcher))
        } else {
            Glide.with(context)
                    .load(model.photoUrl)
                    .into(holder.userImageView)

        }
    }
}