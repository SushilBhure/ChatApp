package com.sushil.chatapp.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.sushil.chatapp.R
import com.sushil.chatapp.databinding.FragmentProfileBinding
import com.sushil.chatapp.databinding.FragmentStatusBinding
import com.sushil.chatapp.ui.MainActivity

class StatusFragment : Fragment(R.layout.fragment_status) {

    private val binding by viewBinding(FragmentStatusBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnMore.setOnClickListener({
            (activity as? MainActivity)?.showLogoutPopupMenu(it)
        })

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            onBackpress()
        }

    }

    fun onBackpress(){
        Log.d("BackPress", "Back pressed in ChatFragment - exiting app")
        findNavController().navigate(
            R.id.chatFragment,
            null,
            NavOptions.Builder()
                .setPopUpTo(R.id.chatFragment, true) // Clear everything including itself
                .build()
        )
    }

}