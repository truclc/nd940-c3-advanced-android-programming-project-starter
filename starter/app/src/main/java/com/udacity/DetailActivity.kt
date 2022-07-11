package com.udacity

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.udacity.MainActivity.Companion.EXTRA_DOWNLOAD_STATUS_KEY
import com.udacity.MainActivity.Companion.EXTRA_FILE_NAME_KEY
import com.udacity.MainActivity.Companion.NOTIFICATION_DOWNLOAD_COMPLETED_ID
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    private val TAG = "DetailActivity"

    /**
     * onCreate
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.cancel(NOTIFICATION_DOWNLOAD_COMPLETED_ID)

        val fileName = intent.getStringExtra(EXTRA_FILE_NAME_KEY)
        val downloadStatus = intent.getBooleanExtra(EXTRA_DOWNLOAD_STATUS_KEY, false)
        Log.d(TAG, "onCreate: [fileName]=  " + fileName + ", [downloadStatus]= " + downloadStatus)

        tv_file_name.text = fileName
        when (downloadStatus) {
            true -> {
                tv_status.setTextColor(getColor(R.color.colorPrimary))
                tv_status.text = getString(R.string.success)
            }
            else -> {
                tv_status.setTextColor(getColor(R.color.red))
                tv_status.text = getString(R.string.fail)
            }
        }

        // Back to Main
        btn_ok.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}
