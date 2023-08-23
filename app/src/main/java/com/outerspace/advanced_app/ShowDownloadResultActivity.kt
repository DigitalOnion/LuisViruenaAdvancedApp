package com.outerspace.advanced_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.outerspace.advanced_app.Constants.NOTIFICATION_EXTRA_FILENAME
import com.outerspace.advanced_app.Constants.NOTIFICATION_EXTRA_LAST_MODIFIED
import com.outerspace.advanced_app.Constants.NOTIFICATION_EXTRA_LENGTH
import com.outerspace.advanced_app.Constants.NOTIFICATION_EXTRA_SUCCESS
import com.outerspace.advanced_app.databinding.ActivityShowDownloadResultBinding
import java.text.DateFormat

import java.util.*

data class ShowDownloadData(
    var fileName: String,
    var fileLength: String,
    var success: String,
    var lastModified: String,
)

class ShowDownloadResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShowDownloadResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_show_download_result)

        val intent = this.intent
        val fileName = intent.getStringExtra(NOTIFICATION_EXTRA_FILENAME) ?: ""
        val fileLength = intent.getIntExtra(NOTIFICATION_EXTRA_LENGTH, 0)
        val success = intent.getStringExtra(NOTIFICATION_EXTRA_SUCCESS) ?: ""
        val lastModified = Date(intent.getLongExtra(NOTIFICATION_EXTRA_LAST_MODIFIED, 0L))

        val df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault())
        val d = df.format(lastModified)
        binding.binding = ShowDownloadData(fileName, fileLength.toString(), success,  d)

        binding.buttonNavigateBack.setOnClickListener{
            this.startActivity(Intent(this, MainActivity::class.java))
        }
    }
}