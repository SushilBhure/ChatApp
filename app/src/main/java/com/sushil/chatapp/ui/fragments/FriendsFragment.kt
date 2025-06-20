package com.sushil.chatapp.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sushil.chatapp.R
import com.sushil.chatapp.adapters.FriendListAdapter
import com.sushil.chatapp.databinding.FragmentChatBinding
import com.sushil.chatapp.ui.MainActivity
import com.sushil.chatapp.utils.PrefManager
import com.sushil.chatapp.viewmodels.UserViewModel


class FriendsFragment : Fragment(R.layout.fragment_chat) {

    private val binding by viewBinding(FragmentChatBinding::bind)

    private val viewModel: UserViewModel by viewModels()
    private lateinit var adapter: FriendListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            Log.d("BackPress", "Back pressed in ChatFragment - exiting app")
            requireActivity().finishAffinity() // exits the app on back press
        }

        adapter = FriendListAdapter(::onItemClick)
        binding.recyclerUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerUsers.adapter = adapter

        viewModel.loadFriendList(PrefManager.getUserId(requireActivity()).toString())

        viewModel.friendsList.observe(viewLifecycleOwner) {
            Log.d( "onViewCreated: ",it.toString())
          //  adapter.submitList(it)
            adapter.submitList(it)
        }

        binding.btnMore.setOnClickListener({
            (activity as? MainActivity)?.showLogoutPopupMenu(it)
        })

    }

    private fun onItemClick(ID: String){
        val action = FriendsFragmentDirections.actionChatFragmentToChatListFragment(
            UserID = ID // Parameter name matches the 'android:name' in nav_graph.xml
        )
        findNavController().navigate(action)
    }
}