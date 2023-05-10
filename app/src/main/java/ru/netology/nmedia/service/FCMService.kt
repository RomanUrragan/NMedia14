package ru.netology.nmedia.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONObject
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import kotlin.random.Random

class FCMService : FirebaseMessagingService() {
    //private val content = "content"
    private val channelId = "remote"
    private val gson = Gson()

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val jsonObject = JSONObject(message.data.values.firstOrNull())
        val recipientId: String? =
            if (jsonObject.isNull("recipientId")) null else jsonObject.optString("recipientId")
        val content: String? = jsonObject.optString("content")
        val authId = AppAuth.getInstance().authStateFlow.value.id

        if (recipientId == null) {
            showNotification(content)
        } else if (recipientId.toLong() != authId
            && recipientId.toInt() == 0
        ) {
            AppAuth.getInstance().sendPushToken()
        } else if (recipientId.toLong() != authId
            && recipientId.toInt() != 0
        ) {
            AppAuth.getInstance().sendPushToken()
        } else if (recipientId.toLong() == authId) {
            showNotification(content)
        }
    }

    private fun showNotification(content: String?) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setStyle(NotificationCompat.BigTextStyle())
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(this)
            .notify(Random.nextInt(100_000), notification)
    }

    override fun onNewToken(token: String) {
        AppAuth.getInstance().sendPushToken(token)
    }
}
