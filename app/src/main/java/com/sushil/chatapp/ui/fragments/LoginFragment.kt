package com.sushil.chatapp.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.sushil.chatapp.repository.AuthRepository
import com.sushil.chatapp.R
import com.sushil.chatapp.databinding.FragmentLoginBinding
import com.sushil.chatapp.ui.showOTPDialog
import com.sushil.chatapp.utils.PrefManager
import com.sushil.chatapp.utils.showToast
import com.sushil.chatapp.viewmodels.AuthViewModel
import com.sushil.chatapp.viewmodels.AuthViewModelFactory
import com.sushil.chatapp.viewmodels.UserViewModel
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.fragment_login) {

    private val binding by viewBinding(FragmentLoginBinding::bind)

    private val userViewModel: UserViewModel by viewModels()

    private val authViewModel: AuthViewModel by  viewModels {
        AuthViewModelFactory(requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.btnNext.setOnClickListener {
          //  findNavController().navigate(R.id.action_loginFragment_to_chatFragment)
          //  findNavController().navigate(R.id.action_loginFragment_to_chatFragment)

        //    showOTPDialog(requireActivity())

          /*  findNavController().navigate(
                R.id.chatFragment,
                null,
                NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true) // or use the root/start destination
                    .build()
            )*/

            lifecycleScope.launch {
            val phone = binding.editTextPhone.text.toString()
            if (phone.isNotBlank() && phone.length==10) {
                val isRegistered = userViewModel.isUserRegistered(number = phone)
               if(isRegistered){
                   binding.progressCircular.visibility= View.VISIBLE
                   binding.btnNext.isEnabled = false
                   binding.layRegister.isEnabled = false
                   authViewModel.sendOtp("+919988776655", requireActivity())
               }else{
                   requireActivity().showToast("Oops! No user registered with this number. Try registering first..")
               }
            }else{
                requireActivity().showToast("Please enter valid 10 digit phone number..")
            }

            }
        }

        binding.layRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            Log.d("BackPress", "Back pressed in LoginFragment - exiting app")
            requireActivity().finishAffinity() // exits the app on back press
        }

        observeViewModel()

    }

    private fun VerifyOTP(otp: String){
        if (otp.isNotEmpty()) {
            authViewModel.verifyCode(otp)

            binding.progressCircular.visibility= View.VISIBLE
            binding.btnNext.isEnabled = false
            binding.layRegister.isEnabled = false
        }
    }

    private fun observeViewModel() {
        authViewModel.codeSent.observe(viewLifecycleOwner) {
            showOTPDialog(requireActivity(),::VerifyOTP)
            binding.progressCircular.visibility= View.GONE
            binding.btnNext.isEnabled = true
            binding.layRegister.isEnabled = true
            Toast.makeText(context, "OTP Sent", Toast.LENGTH_SHORT).show()
        }

        authViewModel.signInSuccess.observe(viewLifecycleOwner) {
            binding.progressCircular.visibility= View.GONE
            binding.btnNext.isEnabled = true
            binding.layRegister.isEnabled = true
            Toast.makeText(context, "Log-in Success!", Toast.LENGTH_SHORT).show()
            PrefManager.saveUserId(requireActivity(), binding.editTextPhone.text.toString())
            // Navigate to next screen
            findNavController().navigate(
                R.id.chatFragment,
                null,
                NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true) // or use the root/start destination
                    .build()
            )
        }

        authViewModel.error.observe(viewLifecycleOwner) {
            binding.progressCircular.visibility= View.GONE
            binding.btnNext.isEnabled = true
            binding.layRegister.isEnabled = true
            Toast.makeText(context, "Error: $it", Toast.LENGTH_SHORT).show()
            Log.e("Firebase_auth_error", it)
        }
    }
}