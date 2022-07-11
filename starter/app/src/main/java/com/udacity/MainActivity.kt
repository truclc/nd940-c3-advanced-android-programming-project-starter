package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private var downloadID: Long = 0
    private var url = ""
    private var title = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        createChannel(
            this,
            CHANNEL_ID,
            getString(R.string.chanel_name),
            getString(R.string.chanel_des)
        )

        custom_button.setOnClickListener {
            custom_button.updateButtonState(ButtonState.Loading)
            when (rd_options.checkedRadioButtonId) {
                INVALID_ID -> {
                    Toast.makeText(
                        this,
                        getString(R.string.warning_select_file),
                        Toast.LENGTH_SHORT
                    ).show()
                    custom_button.updateButtonState(ButtonState.Completed)
                }
                else -> {
                    val checkedId: RadioButton =
                        rd_options.findViewById(rd_options.checkedRadioButtonId)
                    download(checkedId.id)
                }
            }
        }
    }

    /**
     * onDestroy
     */
    override fun onDestroy() {
        super.onDestroy()

        //unregisterReceiver
        unregisterReceiver(receiver)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: 0L
            val action = intent?.action
            if (downloadID != id || !action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) return
            val query = DownloadManager.Query().setFilterById(downloadID)
            val downloadManager =
                getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val cursor = downloadManager.query(query)
            if (cursor.moveToFirst()) {
                if (cursor.count > 0) {
                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    val title =
                        cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE))
                    val success = status == DownloadManager.STATUS_SUCCESSFUL
                    custom_button.updateButtonState(ButtonState.Completed)
                    sendNotification(this@MainActivity, title, success)
                }
            }
            cursor.close()
        }
    }

    /**
     * download
     */
    private fun download(checkedId: Int) {
        setURL(checkedId)
        if (!isNetworkConnected()) {
            sendNotification(this@MainActivity, title, false)
            Handler(Looper.getMainLooper()).postDelayed({
                custom_button.updateButtonState(ButtonState.Completed)
            }, R.dimen.animator_duration.toLong())

        }
        Log.d(TAG, "download: url " + url)

        val request =
            DownloadManager.Request(Uri.parse(url + EX_URL))
                .setTitle(title)
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)
    }

    /**
     * setURL
     */
    private fun setURL(checkedId: Int) {
        when (checkedId) {
            R.id.rb_glide -> {
                url = GLIDE_URL
                title = getString(R.string.glide_string)
            }
            R.id.rb_load_app -> {
                url = LOAD_APP_URL
                title = getString(R.string.load_app_string)

            }
            R.id.rb_retrofit -> {
                url = RETROFIT_URL
                title = getString(R.string.retrofit_string)
            }
        }
    }

    companion object {

        private const val LOAD_APP_URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter"

        private const val GLIDE_URL =
            "https://github.com/bumptech/glide"

        private const val RETROFIT_URL =
            "https://github.com/square/retrofit"

        private const val EX_URL = "/archive/master.zip"

        const val NOTIFICATION_DOWNLOAD_COMPLETED_ID = 0

        const val INVALID_ID = -1

        const val EXTRA_FILE_NAME_KEY = "fileName"

        const val EXTRA_DOWNLOAD_STATUS_KEY = "downloadStatus"

        private const val CHANNEL_ID = "channelId"
    }

    /**
     * sendNotification
     */
    fun sendNotification(context: Context, fileName: String, status: Boolean) {

        val intent = Intent(context, DetailActivity::class.java)
        intent.putExtra(EXTRA_FILE_NAME_KEY, fileName)
        intent.putExtra(EXTRA_DOWNLOAD_STATUS_KEY, status)
        val pendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setSmallIcon(R.drawable.ic_assistant_black_24dp)
            setContentTitle(getString(R.string.notification_title))
            setContentText(getString(R.string.notification_description))
            setContentIntent(pendingIntent)
                .addAction(
                    R.drawable.ic_assistant_black_24dp,
                    context.getString(R.string.notification_button),
                    pendingIntent
                )
            setAutoCancel(true)
        }

        // Show the notification
        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_DOWNLOAD_COMPLETED_ID, notification.build())
    }

    /**
     * createChannel
     */
    private fun createChannel(
        context: Context,
        channelId: String,
        channelName: String,
        channelDescription: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                setShowBadge(false)
                enableVibration(true)
                description = channelDescription
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    /**
     * isNetworkConnected
     */
    fun isNetworkConnected(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }
}
