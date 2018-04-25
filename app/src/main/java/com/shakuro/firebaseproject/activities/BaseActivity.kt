package com.shakuro.firebaseproject.activities

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.ResultReceiver
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.shakuro.firebaseproject.R


open class BaseActivity : AppCompatActivity() {
    private var progressDialog: ProgressDialog? = null

    fun showProgressDialog() {
        if (progressDialog == null || progressDialog?.window == null || progressDialog?.isShowing == false) {
            progressDialog = ProgressDialog(this)
        }

        progressDialog?.let {
            if (!it.isShowing) {
                try {
                    it.show()
                    it.setCancelable(false)
                    it.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    it.setContentView(R.layout.progressdialog_layout)
                } catch (e: WindowManager.BadTokenException) {

                }
            }
        }
    }

    fun hideProgressDialog() {
        if (progressDialog != null && progressDialog?.window != null) {
            progressDialog?.dismiss()
        }
        progressDialog = null
    }

    fun hideKeyboard(resultReceiver: ResultReceiver? = null) {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0, resultReceiver)
        }
    }
}