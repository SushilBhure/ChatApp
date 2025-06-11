package com.sushil.chatapp.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.sushil.chatapp.R
import com.sushil.chatapp.databinding.FragmentLoginBinding
import com.sushil.chatapp.databinding.FragmentRegisterBinding
import com.sushil.chatapp.models.User
import com.sushil.chatapp.repository.AuthRepository
import com.sushil.chatapp.ui.showOTPDialog
import com.sushil.chatapp.utils.ImageUtil
import com.sushil.chatapp.utils.PrefManager
import com.sushil.chatapp.utils.showToast
import com.sushil.chatapp.viewmodels.AuthViewModel
import com.sushil.chatapp.viewmodels.AuthViewModelFactory
import com.sushil.chatapp.viewmodels.UserViewModel
import kotlinx.coroutines.launch


class RegisterFragment : Fragment(R.layout.fragment_register) {

    private val binding by viewBinding(FragmentRegisterBinding::bind)

    private val userViewModel: UserViewModel by viewModels()
    private var imgUrl: String =""

    private val authViewModel: AuthViewModel by  viewModels {
        AuthViewModelFactory(requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init(){

        ImageUtil.register(this) { base64 ->
            if (base64 != null) {
                // Use the base64 string (e.g., load into ImageView)
                Log.d("selectedImage",base64)
                imgUrl = base64
                ImageUtil.loadBase64IntoImageView(imgUrl, binding.circularImage)
            } else {
                // Handle permission denied or error
               // imgUrl = ""
                Log.e("selectedImage","Image selection failed")
            }
        }

        binding.edtImg.setOnClickListener({
            //captureFromCamera()
            //   pickFromGallery()
            ImageUtil.showImagePickerDialog(this)
        })

        binding.btnNext.setOnClickListener {

            lifecycleScope.launch {
                val name = binding.edtName.text.toString()
                if(name.isBlank()){
                    requireActivity().showToast("Please enter your name..")
                }else{
                    val phone = binding.editTextPhone.text.toString()
                    if (phone.isNotBlank() && phone.length==10) {
                        val isRegistered = userViewModel.isUserRegistered(number = phone)
                        if(isRegistered){
                            requireActivity().showToast("An account with this number already exists. Please log in..")
                        }else{
                            binding.progressCircular.visibility= View.VISIBLE
                            binding.btnNext.isEnabled = false
                            binding.layLogin.isEnabled = false
                            authViewModel.sendOtp("+919988776655", requireActivity())
                        }
                    }else{
                        requireActivity().showToast("Please enter valid 10 digit phone number..")
                    }

                }

            }

        }

        binding.layLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            Log.d("BackPress", "Back pressed in RegisterFragment - exiting app")
            requireActivity().finishAffinity() // exits the app on back press
        }

        observeViewModel()

    }


    private fun VerifyOTP(otp: String){
        if (otp.isNotEmpty()) {
            authViewModel.verifyCode(otp)

            binding.progressCircular.visibility= View.VISIBLE
            binding.btnNext.isEnabled = false
            binding.layLogin.isEnabled = false
        }
    }

    private fun observeViewModel() {
        authViewModel.codeSent.observe(viewLifecycleOwner) {
            showOTPDialog(requireActivity(),::VerifyOTP)
            binding.progressCircular.visibility= View.GONE
            binding.btnNext.isEnabled = true
            binding.layLogin.isEnabled = true
            requireActivity().showToast("OTP Sent")
        }

        authViewModel.signInSuccess.observe(viewLifecycleOwner) {
            binding.progressCircular.visibility= View.GONE
            binding.btnNext.isEnabled = true
            binding.layLogin.isEnabled = true
            requireActivity().showToast("Sign-in Success!")
            CreateUser()
            // Navigate to next screen
        }

        authViewModel.error.observe(viewLifecycleOwner) {
            binding.progressCircular.visibility= View.GONE
            binding.btnNext.isEnabled = true
            binding.layLogin.isEnabled = true
            Toast.makeText(context, "Error: $it", Toast.LENGTH_SHORT).show()
            Log.e("Firebase_auth_error", it)
        }
    }

    private fun CreateUser(){
        val number = binding.editTextPhone.text.toString()
        val name = binding.edtName.text.toString()
        userViewModel.createUser(number, User(name=name, number=number, profileImageUrl = imgUrl))

        PrefManager.saveUserId(requireActivity(), number)

        findNavController().navigate(
            R.id.chatFragment,
            null,
            NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true) // or use the root/start destination
                .build()
        )
    }
}