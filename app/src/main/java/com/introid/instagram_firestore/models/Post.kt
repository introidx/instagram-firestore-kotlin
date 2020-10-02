package com.introid.instagram_firestore.models

import com.google.firebase.firestore.PropertyName

data class Post(
    var description : String = "",
    @get:PropertyName("image_url") @set:PropertyName("image_url") var imageUrl : String = "",
    @get:PropertyName("creation_time_ms") @set:PropertyName("creation_time_ms") var creationTimeMs : Long = -1,
    var user : User? = null
)