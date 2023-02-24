package com.outerspace.advanced_app

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FileDownload {
    private lateinit var _url: String
    private lateinit var _progress: MutableLiveData<Float>

    class Factory {
        private val instance: FileDownload by lazy {
            FileDownload()
        }

        fun url(url: String): Factory {
            instance._url = url
            return this
        }

        fun mutableProgress(progress: MutableLiveData<Float>): Factory {
            instance._progress = progress
            return this
        }

        fun build(): FileDownload {
            return instance
        }
    }

    fun download(activity: AppCompatActivity) {
        val request = DownloadManager.Request(Uri.parse(_url))
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"THE_FILE")
        val downloadManager= (activity as Context).getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadID = downloadManager.enqueue(request)

        activity.lifecycleScope.launch {
            var percentage: Float = 0.0F
            var prevPercentage: Float = 0.0F
            var isFinished = false
            while(!isFinished) {
                val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadID))
                if(cursor.moveToFirst()) {
                    val colStatusIdx = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    if (colStatusIdx < 0 ) continue
                    when (cursor.getInt(colStatusIdx)) {           // status
                        DownloadManager.STATUS_RUNNING -> {
                            val colTotalSizeIdx = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                            val totalSize: Float = cursor.getLong(colTotalSizeIdx) * 1.0F
                            if (totalSize > 0) {
                                val colBytesSoFarIdx = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                                val bytesSoFar: Float = cursor.getLong(colBytesSoFarIdx) * 1.0F
                                percentage = bytesSoFar / totalSize
                            }
                        }
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            percentage = 1.0F
                            isFinished = true
                        }
                        DownloadManager.STATUS_FAILED -> {
                            isFinished = true
                        }
                    }
                    if (percentage != prevPercentage) {
                        prevPercentage = percentage
                        _progress.value = percentage        // notify the observer
                        delay(100)
                        Log.d("DOWNLOAD BYTES", "progress = $percentage")
                    }
                }
            }
        }
    }
}

//        var percentage = 0.0F
//        val timer = object: CountDownTimer(3000, 30) {
//            override fun onTick(millisUntilFinished: Long) {
//                percentage += 0.01F
//                _progress.value = percentage
//            }
//            override fun onFinish() {}
//        }
//        timer.start()

