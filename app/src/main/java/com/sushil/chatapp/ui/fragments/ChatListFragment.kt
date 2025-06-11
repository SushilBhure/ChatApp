package com.sushil.chatapp.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.sushil.chatapp.R
import com.sushil.chatapp.databinding.FragmentChatBinding
import com.sushil.chatapp.databinding.FragmentChatListBinding
import com.sushil.chatapp.ui.MainActivity
import com.sushil.chatapp.utils.ImageUtil
import com.sushil.chatapp.utils.PrefManager
import com.sushil.chatapp.viewmodels.UserViewModel

class ChatListFragment : Fragment(R.layout.fragment_chat_list) {

    // Use the navArgs() delegate to easily retrieve arguments
    private val args: ChatListFragmentArgs by navArgs()

    private val userViewModel: UserViewModel by activityViewModels()
    private val binding by viewBinding(FragmentChatListBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init(){
        // Access your string argument directly from the 'args' object
        val friendID: String = args.UserID // Will be String? if nullable, String if not

        if(!friendID.isNullOrBlank()){

            userViewModel.getUser(friendID)

            userViewModel.user.observe(viewLifecycleOwner) {
                if(it!=null){
                    ImageUtil.loadBase64IntoImageView(it.profileImageUrl,binding.frndProfileImg)
                    binding.txtFrndName.text = it.name
                    if(it.onlineStatus){
                        binding.txtFrndStatus.text= "Online"
                        binding.txtFrndStatus.setTextColor(Color.parseColor("#4CBB17"))
                    }else{
                        binding.txtFrndStatus.text= "Offline"
                        binding.txtFrndStatus.setTextColor(Color.parseColor("#636363"))
                    }
                }
            }

        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            onBackpress()
        }

        binding.layBackpress.setOnClickListener({
           onBackpress()
        })

        binding.btnMore.setOnClickListener({
            (activity as? MainActivity)?.showLogoutPopupMenu(it)
        })
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