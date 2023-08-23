package com.outerspace.advanced_app

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FileDownload {
    private lateinit var url: String
    private lateinit var progressConsumer: ProgressConsumerInterface
    private lateinit var downloadCompleted: MutableLiveData<Cursor>

    class Factory {
        private val instance: FileDownload by lazy {
            FileDownload()
        }

        fun fileUrl(url: String): Factory {
            instance.url = url
            return this
        }

        fun addProgressConsumer(progressConsumer: ProgressConsumerInterface): Factory {
            instance.progressConsumer = progressConsumer
            return this
        }

        fun addDownloadCompleted(downloadCompleted: MutableLiveData<Cursor>): Factory {
            instance.downloadCompleted = downloadCompleted
            return this
        }

        fun build(): FileDownload {
            return instance
        }
    }

    fun download(activity: AppCompatActivity) {
        val uri: Uri = Uri.parse(url)
        val filename = uri.lastPathSegment
        val request = DownloadManager.Request(uri)
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)  // VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
        val downloadManager= (activity as Context).getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadID = downloadManager.enqueue(request)

        activity.lifecycleScope.launch {
            progressConsumer.startAt(0.1F)
            var percentage = 0.0F
            var prevPercentage = 0.0F
            while(true) {
                val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadID))
                val colStatusIdx = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                cursor.moveToFirst()
                if (colStatusIdx >= 0 ) {
                    val colTotalSizeIdx = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    val totalSize: Float = cursor.getLong(colTotalSizeIdx) * 1.0F
                    if (totalSize <= 0) continue
                    val colBytesSoFarIdx = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val bytesSoFar: Float = cursor.getLong(colBytesSoFarIdx) * 1.0F
                    percentage = bytesSoFar / totalSize
                    if (percentage != prevPercentage) {
                        prevPercentage = percentage
                        progressConsumer.progress(percentage)
                    }
                    when (cursor.getInt(colStatusIdx)) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            progressConsumer.finish()
                            downloadCompleted.value = cursor
                            break
                        }
                        DownloadManager.STATUS_FAILED -> {
                            progressConsumer.error(R.string.download_error)
                            downloadCompleted.value = cursor
                            break
                        }
                    }
                    delay(50)
                }
            }
        }
    }
}


