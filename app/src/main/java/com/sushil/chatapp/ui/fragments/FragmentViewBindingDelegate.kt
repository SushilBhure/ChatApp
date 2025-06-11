package com.sushil.chatapp.ui.fragments

import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class FragmentViewBindingDelegate<T : ViewBinding>(
    val bindingInflater: (View) -> T
) : ReadOnlyProperty<Fragment, T> {

    private var binding: T? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        val view = thisRef.view ?: throw IllegalStateException("View is not created yet")
        return binding ?: bindingInflater(view).also { binding = it }
    }
}

fun <T : ViewBinding> Fragment.viewBinding(bindingInflater: (View) -> T) =
    FragmentViewBindingDelegate(bindingInflater)