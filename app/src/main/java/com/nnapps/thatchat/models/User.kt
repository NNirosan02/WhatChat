package com.nnapps.thatchat.models

import android.net.Uri
import com.google.firebase.firestore.PropertyName

data class User(
    @PropertyName("profilePic")val profilePic: Uri?,
    @PropertyName("userName")val userName: String?,
    @PropertyName("mail")val mail: String?,
    @PropertyName("userId")val userId: String?,
    @PropertyName("password")val password: String?,
    @PropertyName("lastMessage")val lastMessage: String?,
    @PropertyName("status")val status: String?
)
