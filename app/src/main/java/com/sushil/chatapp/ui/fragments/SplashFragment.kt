package com.sushil.chatapp.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.sushil.chatapp.R
import com.sushil.chatapp.databinding.FragmentSplashBinding
import com.sushil.chatapp.viewmodels.AuthViewModel
import com.sushil.chatapp.viewmodels.AuthViewModelFactory

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment(R.layout.fragment_splash) {


    private val binding by viewBinding(FragmentSplashBinding::bind)

    private val authViewModel: AuthViewModel by  activityViewModels {
        AuthViewModelFactory(requireActivity())
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            delay(3000L) // 3 seconds

            authViewModel.isConnected.observe(viewLifecycleOwner) {
                if(it){
                    if(authViewModel.getUserSession()){
                        findNavController().navigate(R.id.action_splashFragment_to_chatFragment)
                    }else{
                        findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
                    }
                }

            }

        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            Log.d("BackPress", "Back pressed in SplashFragment - exiting app")
            requireActivity().finishAffinity() // exits the app on back press
        }
    }

}