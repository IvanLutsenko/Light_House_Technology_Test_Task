package ru.awac.lighthousetechnologytesttask

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.isVisible
import com.google.android.gms.auth.api.phone.SmsRetriever

interface NotificationReceiver {
    fun addNotification(notification: ReceivedNotification)
}

class MainActivity : AppCompatActivity(), NotificationReceiver {

    private lateinit var smsBroadcastReceiver: SmsBroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)) {
            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            startActivity(intent)
        }

        startSmsUserConsent()
    }

    override fun onStart() {
        super.onStart()
        registerToSmsBroadcastReceiver()
    }

    override fun onResume() {
        super.onResume()
        applicationContext.stopService(Intent(applicationContext, NotificationService::class.java))
        val newService = NotificationService()
        newService.setListener(this)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(smsBroadcastReceiver)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_USER_CONSENT -> {
                if ((resultCode == Activity.RESULT_OK) && (data != null)) {
                    //That gives all message to us. We need to get the code from inside with regex
                    val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                    val code = message?.let { fetchVerificationCode(it) }
                    val otpTV = findViewById<TextView>(R.id.otp)

                    otpTV.text = code
                }
            }
        }
    }

    override fun addNotification(notification: ReceivedNotification) {
        val notificationLayout = findViewById<ConstraintLayout>(R.id.notification)
        val notificationPackageName = findViewById<TextView>(R.id.textViewPackageName)
        val notificationPostTime = findViewById<TextView>(R.id.textViewPostTime)
        val notificationText = findViewById<TextView>(R.id.textViewText)
        val notificationTitle = findViewById<TextView>(R.id.textViewTitle)

        notificationLayout.isVisible = true
        with(notification) {
        notificationPackageName.text = packageName
        notificationPostTime.text = resources.getString(
            R.string.notification_postTime,
            time,
            date
        )
            notificationText.text = text
            notificationTitle.text = title
        }
    }

    private fun startSmsUserConsent() {
        SmsRetriever.getClient(this).also {
            //We can add user phone number or leave it blank
            it.startSmsUserConsent(null)
                .addOnSuccessListener {
                    Log.d(TAG, "LISTENING_SUCCESS")
                }
                .addOnFailureListener {
                    Log.d(TAG, "LISTENING_FAILURE")
                }
        }
    }

    private fun registerToSmsBroadcastReceiver() {
        smsBroadcastReceiver = SmsBroadcastReceiver().also {
            it.smsBroadcastReceiverListener = object : SmsBroadcastReceiver.SmsBroadcastReceiverListener {
                override fun onSuccess(intent: Intent?) {
                    intent?.let { context -> startActivityForResult(context, REQ_USER_CONSENT) }
                }

                override fun onFailure() {
                }
            }
        }

        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        registerReceiver(smsBroadcastReceiver, intentFilter)
    }

    private fun fetchVerificationCode(message: String): String {
        return Regex("(\\d{4})").find(message)?.value ?: ""
    }

    companion object {
        const val TAG = "SMS_USER_CONSENT"

        const val REQ_USER_CONSENT = 100
    }
}