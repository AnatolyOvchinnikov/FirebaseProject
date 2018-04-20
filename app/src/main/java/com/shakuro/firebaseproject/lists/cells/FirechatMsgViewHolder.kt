package com.shakuro.firebaseproject.lists.cells

import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.shakuro.firebaseproject.R
import com.shakuro.firebaseproject.activities.PostDetailsActivity


class FirechatMsgViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    var postTitleTextView: TextView
    var postDescriptionTextView: TextView
    var postImageView: ImageView

    lateinit var id: String

    init {
        postTitleTextView = itemView.findViewById(R.id.postTitleTextView) as TextView
        postDescriptionTextView = itemView.findViewById(R.id.postDescriptionTextView) as TextView
        postImageView = itemView.findViewById(R.id.postImageView) as ImageView

        itemView.setOnClickListener {
            ContextCompat.startActivity(itemView.context, Intent(itemView.context,
                    PostDetailsActivity::class.java).apply {
                putExtra(PostDetailsActivity.POST_ID, id)
            }, null)
        }
    }
}