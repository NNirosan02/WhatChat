package com.nnapps.thatchat.ui.users

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nnapps.thatchat.R
import com.nnapps.thatchat.databinding.ItemChatUserBinding
import com.nnapps.thatchat.models.User
import com.squareup.picasso.Picasso

class UserAdapter(var usersList: MutableList<User>) :
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemChatUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentUser = usersList[position]
        Picasso.get().load(currentUser.profilePic).placeholder(R.drawable.ic_launcher_foreground).into(holder.binding.profileImage)
        holder.binding.userName.text = currentUser.userName

    }

    override fun getItemCount(): Int {
        return usersList.size
    }

    inner class ViewHolder(val binding: ItemChatUserBinding) : RecyclerView.ViewHolder(binding.root)
}