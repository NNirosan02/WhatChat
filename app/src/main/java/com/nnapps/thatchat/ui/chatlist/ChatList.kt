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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.nnapps.thatchat.R
import com.nnapps.thatchat.databinding.ChatListFragmentBinding
import com.nnapps.thatchat.models.User
import com.nnapps.thatchat.ui.users.UserAdapter


class ChatList : Fragment(), UserAdapter.ChatItemClickListener {

    private lateinit var chatListBinding: ChatListFragmentBinding
    private lateinit var viewModel: ChatListViewModel
    private lateinit var database: CollectionReference
    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
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

        initializeRecyclerView()
        setHasOptionsMenu(true)
        chatListBinding.rvChatList.adapter
        return chatListBinding.root
    }

    private fun initializeRecyclerView() {
        adapter = UserAdapter(list, this)
        linearLayoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        chatListBinding.rvChatList.layoutManager = linearLayoutManager
        chatListBinding.rvChatList.adapter = adapter
        database.addSnapshotListener { snapshots, e ->
            println("SNAPSHOT IS ${snapshots!!.documents[0]}")
            if (e != null) {
                println("ERROR IS $e")
                return@addSnapshotListener
            }
            for (dc in snapshots.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.MODIFIED -> {
                        list.clear()
                        snapshots.forEach { dataSnapshot ->
                            val obj: Any = dataSnapshot.data
                            val newUser: User = obj as User
                            list.add(newUser)
                        }
                    }
                    DocumentChange.Type.ADDED -> {
                        list.clear()
                        snapshots.forEach { dataSnapshot ->
                            val gson = Gson()
                            val jsonElement: JsonElement = gson.toJsonTree(dataSnapshot.data)
                            val user: User = gson.fromJson(jsonElement, User::class.java)
                            if (user.userId != (FirebaseAuth.getInstance().uid)) {
                                list.add(user)
                            }
                            adapter.notifyDataSetChanged()
                        }
                    }
                    DocumentChange.Type.REMOVED -> TODO()
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

    override fun onChatItemClickListener(user: User) {
        val navController = findNavController()
        navController.navigate(R.id.action_chatList_to_chatFragment)
    }


}