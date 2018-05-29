package com.shakuro.firebaseproject.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.ResultReceiver
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.view.Gravity
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.shakuro.firebaseproject.R
import com.shakuro.firebaseproject.utils.ScreenUtils
import com.shakuro.firebaseproject.utils.StringUtils
import java.lang.ref.WeakReference


open class BaseActivity : AppCompatActivity() {
    private var progressDialog: ProgressDialog? = null
    private var previousToastWeakReference: WeakReference<Toast>? = null

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

    fun showAlertDialog(title: String?, message: String, listener: DialogInterface.OnClickListener) {
        val builder = AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT)
        builder.setTitle(title)
                .setMessage(message)
                .setNeutralButton(R.string.ok, listener)
                .setCancelable(false)
                .create()
                .show()
    }

    fun showError(error: Throwable?) {
        if (error != null) {
            showError(error.localizedMessage)
        }
    }

    fun showError(errorMessage: String) {
        showMessage(errorMessage, Gravity.TOP, Toast.LENGTH_LONG, 0, 45)
    }

    fun showMessage(message: String) {
        if (StringUtils.isEmpty(message)) {
            return
        }

        val previous = if (previousToastWeakReference != null) previousToastWeakReference?.get() else null
        if (previous != null) {
            previous!!.cancel()
        }

        val toastText = if (StringUtils.isHtml(message)) Html.fromHtml(message) else message
        val toast = Toast.makeText(this, toastText, Toast.LENGTH_LONG)
        toast.show()

        previousToastWeakReference = WeakReference(toast)
    }

    fun showMessage(message: String, gravity: Int, duration: Int, xOffsetDp: Int, yOffsetDp: Int) {
        if (StringUtils.isEmpty(message)) {
            return
        }

        val previous = if (previousToastWeakReference != null) previousToastWeakReference?.get() else null
        if (previous != null) {
            previous!!.cancel()
        }

        val toastText = if (StringUtils.isHtml(message)) Html.fromHtml(message) else message
        val toast = Toast.makeText(this, toastText, duration)
        toast.setGravity(gravity, ScreenUtils.convertDpToPixels(xOffsetDp.toFloat()), ScreenUtils.convertDpToPixels(yOffsetDp.toFloat()))
        toast.show()

        previousToastWeakReference = WeakReference(toast)
    }
}