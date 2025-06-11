package com.sushil.chatapp.ui.fragments

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
import com.bumptech.glide.Glide
import com.sushil.chatapp.R
import com.sushil.chatapp.databinding.FragmentChatListBinding
import com.sushil.chatapp.databinding.FragmentProfileBinding
import com.sushil.chatapp.ui.MainActivity
import com.sushil.chatapp.utils.ImageUtil
import com.sushil.chatapp.utils.PrefManager
import com.sushil.chatapp.utils.showToast
import com.sushil.chatapp.viewmodels.UserViewModel

class ProfileFragment : Fragment(R.layout.fragment_profile) {


    private val userViewModel: UserViewModel by activityViewModels()

    private val binding by viewBinding(FragmentProfileBinding::bind)

    private val name =""
    private val number=""
    private val imgUrl=""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

       init()

    }

    private fun init(){

        binding.txtEditImg.setOnClickListener({
            //captureFromCamera()
         //   pickFromGallery()
            ImageUtil.showImagePickerDialog(this)
        })

        ImageUtil.register(this) { base64 ->
            if (base64 != null) {
                // Use the base64 string (e.g., load into ImageView)
                Log.d("selectedImage",base64)
                ImageUtil.loadBase64IntoImageView(base64, binding.profileImageView)
                userViewModel.updateUserProfilePic(PrefManager.getUserId(requireActivity()).toString()
                    ,base64)

            } else {
                // Handle permission denied or error
                Log.e("selectedImage","Image selection failed")
            }
        }

       /* userViewModel.getUser(PrefManager.getUserId(requireActivity()).toString())
            .observe(viewLifecycleOwner) { user ->
            user?.let {
                requireActivity().showToast("view model called..")
                ImageUtil.loadBase64IntoImageView(it.profileImageUrl,binding.profileImageView)
                binding.edtName.setText(it.name)
                binding.txtNumber.text = "+91 ${it.number}"
            }
        }*/

        userViewModel.getUser(PrefManager.getUserId(requireActivity()).toString())
        userViewModel.user.observe(viewLifecycleOwner) {
            if(it!=null){
                ImageUtil.loadBase64IntoImageView(it.profileImageUrl,binding.profileImageView)
                binding.edtName.setText(it.name)
                binding.txtNumber.text = "+91 ${it.number}"
            }
        }

        binding.layEdtName.setOnClickListener({
            if(binding.edtName.isEnabled){
                if(binding.edtName.text.isBlank()){
                    requireActivity().showToast("Please enter valid name..")
                }else {
                    binding.edtName.isEnabled = false
                    binding.imgName.setImageResource(R.drawable.edit_icon)
                    userViewModel.updateUserName(PrefManager.getUserId(requireActivity()).toString(),binding.edtName.text.toString())
                }

            }else{
                binding.edtName.isEnabled = true
                binding.imgName.setImageResource(R.drawable.save_it_icon)
            }
        })

        binding.layLogout.setOnClickListener({
            (activity as? MainActivity)?.logout()
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