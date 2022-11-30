package ru.awac.lighthousetechnologytesttask

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlin.random.Random

class BootBroadcastReceiver : BroadcastReceiver() {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        val startServiceIntent = Intent(context, NotificationService::class.java)
        context.startService(startServiceIntent)
    }
}

class NotificationService : NotificationListenerService() {

    companion object {
        var receiver: NotificationReceiver? = null
    }

    fun setListener(receiver: NotificationReceiver?) {
        Companion.receiver = receiver
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("NotificationService", "successfully started")
        startService(Intent(applicationContext, NotificationService::class.java))
    }

    /***
     * voláno při nové notifikaci
     */
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val newNotification = ReceivedNotification(
            null,
            (if(Build.VERSION.SDK_INT >= 29){
                sbn.uid
            } else sbn.id + Random.nextInt(0, 999)),
            sbn.packageName,
            sbn.postTime,
            (sbn.notification.extras.getString(Notification.EXTRA_TITLE) ?: "Unknown title"),
            (sbn.notification.extras.getString(Notification.EXTRA_TEXT) ?: "Unknown text"),
            false
        )
        receiver?.addNotification(newNotification)
        Log.d("onNotificationPosted", "triggered")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

}