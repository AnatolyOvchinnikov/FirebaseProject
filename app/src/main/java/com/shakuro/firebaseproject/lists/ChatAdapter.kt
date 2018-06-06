package com.shakuro.firebaseproject.lists

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.storage.FirebaseStorage
import com.shakuro.firebaseproject.R
import com.shakuro.firebaseproject.entity.PostItem
import kotlinx.android.synthetic.main.post_item_layout.view.*


open class ChatAdapter(val context: Context, options: FirebaseRecyclerOptions<PostItem>,
                       private val onCellClick: (PostItem) -> Unit) : FirebaseRecyclerAdapter<PostItem, ChatAdapter.FirechatMsgViewHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FirechatMsgViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.post_item_layout, parent, false)

        return FirechatMsgViewHolder(view)
    }

    override fun onBindViewHolder(holder: FirechatMsgViewHolder, position: Int, model: PostItem) = holder.bind(model)

    inner class FirechatMsgViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        lateinit var item: PostItem

        init {
            itemView.setOnClickListener {
                onCellClick(item)
            }
        }

        fun bind(postItem: PostItem) {
            item = postItem
            itemView.postTitleTextView.setText(postItem.title)
            itemView.postDescriptionTextView.setText(postItem.description)

            val imageUrl = postItem.photoUrl
            if (imageUrl == null) {
                itemView.postImageView
                        .setImageDrawable(ContextCompat
                                .getDrawable(context,
                                        R.mipmap.ic_launcher))
            } else if (imageUrl.startsWith("gs://") == true) {
                val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                storageReference.getDownloadUrl().addOnCompleteListener {
                    if (it.isSuccessful()) {
                        val downloadUrl = it.getResult().toString()
                        Glide.with(itemView.postImageView.getContext())
                                .load(downloadUrl)
                                .into(itemView.postImageView)
                    } else {
                        Log.w(this.javaClass.simpleName, "Getting download url was not successful.",
                                it.getException())
                    }
                }
            } else {
                Glide.with(context)
                        .load(imageUrl)
                        .into(itemView.postImageView)
            }
        }
    }
}