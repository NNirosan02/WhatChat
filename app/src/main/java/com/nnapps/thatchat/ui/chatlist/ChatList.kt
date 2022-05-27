package com.nnapps.thatchat.ui.chatlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.nnapps.thatchat.R
import com.nnapps.thatchat.databinding.ChatListFragmentBinding
import com.nnapps.thatchat.models.User
import com.nnapps.thatchat.ui.users.UserAdapter

class ChatList : Fragment() {

    private lateinit var chatListBinding: ChatListFragmentBinding
    private lateinit var users: MutableList<User>
    private lateinit var viewModel: ChatListViewModel
    private lateinit var database: CollectionReference
    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private var list = mutableListOf<User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mFirestore = FirebaseFirestore.getInstance()
        mFirestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        navController = findNavController()
        auth = FirebaseAuth.getInstance()
        database = mFirestore.collection("Users")
        chatListBinding = ChatListFragmentBinding.inflate(layoutInflater)
        adapter = UserAdapter(list)
        chatListBinding.rvChatList.adapter

        setHasOptionsMenu(true)
        return chatListBinding.root
    }

    private fun initializeRecyclerView() {
        adapter = UserAdapter(list)
        linearLayoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        chatListBinding.rvChatList.layoutManager = linearLayoutManager
        chatListBinding.rvChatList.adapter = adapter
        database.addSnapshotListener { snapshots, e ->
            if (e != null) {
                return@addSnapshotListener
            }

            for (dc in snapshots!!.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.MODIFIED -> {
                        list.clear()
                        snapshots.forEach { dataSnapshot ->
                            println("HERE's your data ${dataSnapshot.data.entries}")
                        }
                    }
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ChatListViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                auth.signOut()
                navController.navigate(R.id.action_chatList_to_firstFragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}