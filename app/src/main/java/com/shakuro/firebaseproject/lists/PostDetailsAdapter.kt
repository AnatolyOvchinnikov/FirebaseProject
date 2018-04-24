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
import com.shakuro.firebaseproject.entity.CommentItem
import com.shakuro.firebaseproject.entity.PostItem
import com.shakuro.firebaseproject.extentions.timestampToDate
import kotlinx.android.synthetic.main.comment_item_layout.view.*
import kotlinx.android.synthetic.main.post_item_layout.view.*



class PostDetailsAdapter(val context: Context, val options: FirebaseRecyclerOptions<Any>, val postItem: PostItem) : FirebaseRecyclerAdapter<Any, RecyclerView.ViewHolder>(options) {
    private val viewTypes = arrayListOf(FirechatMsgViewHolder::class.java, CommentViewHolder::class.java)

    override fun getItemViewType(position: Int): Int {
        var viewType: Class<out RecyclerView.ViewHolder>? = CommentViewHolder::class.java
        if(position == 0) {
            viewType = FirechatMsgViewHolder::class.java
        }
        return viewTypes.indexOf(viewType)
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val aClass = viewTypes[viewType]
        if(aClass.equals(FirechatMsgViewHolder::class.java)) {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.post_item_layout, parent, false)

            return FirechatMsgViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.comment_item_layout, parent, false)

            return CommentViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: Any) {
        if(holder is CommentViewHolder) {
            holder.bind(model as CommentItem)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is FirechatMsgViewHolder) {
            holder.bind(postItem)
        } else if(holder is CommentViewHolder) {
            super.onBindViewHolder(holder, position - 1)
        }

    }

    inner class FirechatMsgViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        lateinit var item: PostItem

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

    inner class CommentViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        fun bind(commentItem: CommentItem) {
            itemView.apply {
                this.userTextView.text = commentItem.userName
                this.messageTextView.text = commentItem.message
                this.messageTimestamp.text = (commentItem.timestamp as Long).timestampToDate()
            }
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount()
    }
}