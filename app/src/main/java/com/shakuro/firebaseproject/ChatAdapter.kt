package com.shakuro.firebaseproject

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.storage.FirebaseStorage


class ChatAdapter(val context: Context, options: FirebaseRecyclerOptions<PostItem>) : FirebaseRecyclerAdapter<PostItem, FirechatMsgViewHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FirechatMsgViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.post_item_layout, parent, false)

        return FirechatMsgViewHolder(view)
    }

    override fun onBindViewHolder(holder: FirechatMsgViewHolder, position: Int, model: PostItem) {
        holder.postTitleTextView.setText(model.title)
        holder.postDescriptionTextView.setText(model.description)

        val imageUrl = model.photoUrl
        if (imageUrl == null) {
            holder.postImageView
                    .setImageDrawable(ContextCompat
                            .getDrawable(context,
                                    R.mipmap.ic_launcher))
        } else if (imageUrl.startsWith("gs://") == true) {
            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
            storageReference.getDownloadUrl().addOnCompleteListener {
                if (it.isSuccessful()) {
                    val downloadUrl = it.getResult().toString()
                    Glide.with(holder.postImageView.getContext())
                            .load(downloadUrl)
                            .into(holder.postImageView)
                } else {
                    Log.w(this.javaClass.simpleName, "Getting download url was not successful.",
                            it.getException())
                }
            }
        } else {
            Glide.with(context)
                    .load(imageUrl)
                    .into(holder.postImageView)
        }
    }
}