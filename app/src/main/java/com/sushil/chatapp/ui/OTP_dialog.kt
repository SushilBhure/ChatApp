package com.sushil.chatapp.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.sushil.chatapp.R
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import java.util.logging.Handler


@SuppressLint("MissingInflatedId")
fun showOTPDialog(activity: Activity, action: (otp: String) -> Unit){


    val dialog = Dialog(activity, R.style.FullScreenDialog)
    dialog.setContentView(R.layout.otp_dialog_lay)

    val otpEditText: EditText = dialog.findViewById(R.id.edtOTP)
    val btnDone:Button = dialog.findViewById(R.id.btnDone)
    btnDone.isEnabled = false

    Thread(){
        Thread.sleep(2000)
        activity.runOnUiThread({
            otpEditText.setText("123456")
            btnDone.isEnabled = true
        })

    }.start()


    btnDone.setOnClickListener { dialog.dismiss()
        action(otpEditText.text.toString())
    }

   dialog.setCancelable(false)
    dialog.show()

}