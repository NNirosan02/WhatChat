package com.nnapps.thatchat.models

import android.net.Uri
import android.os.Parcelable
import com.google.firebase.firestore.PropertyName
import com.google.gson.annotations.SerializedName

data class User(
    @PropertyName("profilePic")val profilePic: String?,
    @PropertyName("userName")val userName: String?,
    @PropertyName("mail")val mail: String?,
    @PropertyName("userId")val userId: String?,
    @PropertyName("password")val password: String?,
    @PropertyName("lastMessage")val lastMessage: String?,
    @PropertyName("status")val status: String?
)