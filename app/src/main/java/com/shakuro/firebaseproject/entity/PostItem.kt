package com.shakuro.firebaseproject.entity

import java.io.Serializable

data class PostItem(var id: String? = null, var photoUrl: String? = null, val title: String? = null, val description: String? = null) : Serializable