package com.shakuro.firebaseproject.entity

data class CommentItem(var id: String? = null, var userId: String? = null,
                       var userName: String? = null, var message: String? = null, var timestamp: Any? = null)