package com.example.firebase_database

import com.google.firebase.database.IgnoreExtraProperties


@IgnoreExtraProperties
data class User(val userid: Int? = null, val username: String? = null, val email: String? = null) {
        // Null default values create a no-argument default constructor, which is needed
        // for deserialization from a DataSnapshot.
}

