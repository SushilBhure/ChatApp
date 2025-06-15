package com.sushil.chatapp.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.view.WindowManager
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.sushil.chatapp.R
import com.sushil.chatapp.adapters.ChatMessageAdapter
import com.sushil.chatapp.databinding.FragmentChatListBinding
import com.sushil.chatapp.ui.MainActivity
import com.sushil.chatapp.utils.ChatUtils
import com.sushil.chatapp.utils.ImageUtil
import com.sushil.chatapp.utils.PrefManager
import com.sushil.chatapp.utils.hideKeyboard
import com.sushil.chatapp.utils.showToast
import com.sushil.chatapp.viewmodels.ChatViewModel
import com.sushil.chatapp.viewmodels.UserViewModel

class ChatsFragment : Fragment(R.layout.fragment_chat_list) {

    // Use the navArgs() delegate to easily retrieve arguments
    private val args: ChatsFragmentArgs by navArgs()

    private var isOnline = false
    private var isTyping = false
    private var friendID: String=""
    private var chatId: String=""

    private val userViewModel: UserViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()
    private val binding by viewBinding(FragmentChatListBinding::bind)
    private lateinit var adapter: ChatMessageAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init(){
        // Access your string argument directly from the 'args' object
         friendID = args.UserID // Will be String? if nullable, String if not
        val currenUID: String = PrefManager.getUserId(requireActivity()).toString()

        if(!friendID.isNullOrBlank()){

            adapter = ChatMessageAdapter(currenUID)
            val layoutManager = LinearLayoutManager(requireContext())
           // layoutManager.stackFromEnd = true
            binding.chatRecycler.layoutManager = layoutManager
            binding.chatRecycler.adapter = adapter

            binding.chatRecycler.requestFocus()

            chatId = ChatUtils.generateChatId(friendID, currenUID)

            chatViewModel.observeChat(chatId,currenUID)

            chatViewModel.messages.observe(viewLifecycleOwner) { messages ->
                adapter.submitList(messages) {
                    binding.chatRecycler.scrollToPosition(messages.size - 1)
                }
            }

            userViewModel.getUser(friendID)
                .observe(viewLifecycleOwner) { user ->
                    user?.let {
                        ImageUtil.loadBase64IntoImageView(it.profileImageUrl,binding.frndProfileImg)
                        binding.txtFrndName.text = it.name
                        isOnline = it.onlineStatus
                        updateFriendsStatus()

                        /*if(it.onlineStatus){
                            binding.txtFrndStatus.text= "Online"
                            binding.txtFrndStatus.setTextColor(Color.parseColor("#4CBB17"))
                        }else{
                            binding.txtFrndStatus.text= "Offline"
                            binding.txtFrndStatus.setTextColor(Color.parseColor("#636363"))
                        }*/
                    }
                }

            chatViewModel.observeTyping(chatId, currenUID, viewLifecycleOwner)

            chatViewModel.typingStatus.observe(viewLifecycleOwner) { isTyping ->
                this.isTyping = isTyping
                updateFriendsStatus()
            }

           /* binding.edtTypeMsg.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus && binding.edtTypeMsg.isEnabled) {
                    requireActivity().showToast("Focus enabled")
                    chatViewModel.setTyping(chatId, friendID, true)
                } else {
                    requireActivity().showToast("Focus lost")
                    chatViewModel.setTyping(chatId, friendID, false)
                }
            }*/

            binding.edtTypeMsg.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // Called *before* the text is changed
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // Called *as* the text is changing
                    Log.d("EditText", "User is typing: $s")
                    if (s.isNullOrBlank()){
                        chatViewModel.setTyping(chatId, friendID, false)
                    }else{
                        chatViewModel.setTyping(chatId, friendID, true)
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    // Called *after* the text is changed
                    Log.d("EditText", "Final input: $s")
                }
            })

            binding.btnSend.setOnClickListener({
                val msg = binding.edtTypeMsg.text.toString()
                if (msg.isNotEmpty()) {
                    // Send the message
                    chatViewModel.sendMessage(currenUID, friendID, msg)
                    // Clear the input
                    binding.edtTypeMsg.text?.clear()
                    // Close the keyboard
                    binding.edtTypeMsg.hideKeyboard()
                }else{
                    requireActivity().showToast("Please type your message first..")
                }

            })

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

    private fun updateFriendsStatus(){
        if(!isOnline){
            binding.txtFrndStatus.text= "Offline"
            binding.txtFrndStatus.setTextColor(Color.parseColor("#636363"))
        }else{
            if(isTyping){
                binding.txtFrndStatus.text= "Typing..."
                binding.txtFrndStatus.setTextColor(Color.parseColor("#F28C28"))
            }else{
                binding.txtFrndStatus.text= "Online"
                binding.txtFrndStatus.setTextColor(Color.parseColor("#4CBB17"))
            }

        }
    }

    override fun onResume() {
        super.onResume()
        if(binding.edtTypeMsg.text.toString().isNotEmpty()){
            chatViewModel.setTyping(chatId, friendID, true)
        }
    }

    override fun onPause() {
        super.onPause()
        if(!chatId.isNullOrBlank() && !friendID.isNullOrBlank()){
            chatViewModel.setTyping(chatId, friendID, false)
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