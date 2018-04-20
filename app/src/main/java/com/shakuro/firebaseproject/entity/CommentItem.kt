package com.shakuro.firebaseproject.entity

data class CommentItem(var id: Long? = null, var userId: String, var userName: String?, var message: String, var timestamp: Long)