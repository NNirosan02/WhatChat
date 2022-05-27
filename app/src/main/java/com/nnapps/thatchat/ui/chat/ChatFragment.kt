package com.nnapps.thatchat.ui.chat

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import com.nnapps.thatchat.R
import com.nnapps.thatchat.databinding.ChatFragmentBinding
import com.nnapps.thatchat.databinding.ChatListFragmentBinding

class ChatFragment : Fragment() {

    private lateinit var chatFragmentBinding: ChatFragmentBinding
    private lateinit var viewModel: ChatViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        chatFragmentBinding = ChatFragmentBinding.inflate(layoutInflater)
        return chatFragmentBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }
}