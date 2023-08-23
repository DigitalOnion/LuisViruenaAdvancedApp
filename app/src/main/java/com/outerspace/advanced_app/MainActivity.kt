package com.outerspace.advanced_app

import android.Manifest
import android.app.*
import android.app.DownloadManager.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.Cursor.*
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.outerspace.advanced_app.databinding.ActivityMainBinding

const val CHANNEL_ID = "UDACITY-NOTIFICATIONS"

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()

        viewModel.downloadCompleted.observe(this) { cursor: Cursor ->
            when (cursorValue(cursor, COLUMN_STATUS)) {
                STATUS_SUCCESSFUL -> notifySuccess(cursor)
                STATUS_FAILED -> notifyError(cursor)
                else -> {}
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.viewModel = viewModel
        binding.progressButton.lifecycleOwner = this
        binding.progressButton.setOnClickListener {
            binding.progressButton.reset()
            if(viewModel.selectedUrl != null) {
                FileDownload.Factory()
                    .fileUrl(viewModel.selectedUrl!!)
                    .addProgressConsumer(binding.progressButton)
                    .addDownloadCompleted(viewModel.downloadCompleted)
                    .build()
                    .download(this)
            } else {
                Toast.makeText(this, R.string.select_a_file, Toast.LENGTH_LONG).show()
            }
        }
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        viewModel.addButtons(binding.radioGroupView, this@MainActivity)
        binding.radioGroupView.setOnCheckedChangeListener(viewModel.onCheckedListener)
        binding.progressButton.reset()
    }

    // Credits: Taken from: https://developer.android.com/develop/ui/views/notifications/build-notification
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun notifySuccess(cursor: Cursor) {
        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val notificationId = 1000   // no need to save, notification will not be removed or updated
                notify(notificationId, buildNotification(cursor))
            } else{
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
        }
    }

    private fun notifyError(cursor: Cursor) {

    }

    private fun cursorValue(cursor: Cursor, colName: String) : Any? {
        val colIdx= cursor.getColumnIndex(colName)
        if (colIdx < 0) return null
        return when (cursor.getType(colIdx)) {
            FIELD_TYPE_NULL -> null
            FIELD_TYPE_FLOAT -> cursor.getFloat(colIdx)
            FIELD_TYPE_INTEGER -> cursor.getInt(colIdx)
            FIELD_TYPE_STRING -> cursor.getString(colIdx)
            else -> null
        }
    }

    private fun buildNotification(cursor: Cursor): Notification {
        val uri: Uri = Uri.parse(cursorValue(cursor, COLUMN_LOCAL_URI) as String)
        val filename = uri.lastPathSegment ?: this.getString(R.string.unknownFilename)
        val status = if(cursorValue(cursor, COLUMN_STATUS) as Int == STATUS_SUCCESSFUL) this.getString(R.string.downloadSuccess) else this.getString(R.string.downloadFail)
        val timestamp: Long = 0L + (cursorValue(cursor, COLUMN_LAST_MODIFIED_TIMESTAMP)) as Int
        val len = cursorValue(cursor, COLUMN_TOTAL_SIZE_BYTES) as Int

        val intent = Intent(this, ShowDownloadResultActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(Constants.NOTIFICATION_EXTRA_FILENAME, filename)
            putExtra(Constants.NOTIFICATION_EXTRA_SUCCESS, status)
            putExtra(Constants.NOTIFICATION_EXTRA_LAST_MODIFIED, timestamp)
            putExtra(Constants.NOTIFICATION_EXTRA_LENGTH, len)
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(this.getString(R.string.notificationTitle))
            .setContentText(this.getString(R.string.notificationText))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(R.drawable.ic_launcher_foreground, getString(R.string.notificationButtonFace), pendingIntent)
            //.setContentIntent(pendingIntent)

        return builder.build()
    }
}