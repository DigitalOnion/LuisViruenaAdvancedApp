package com.outerspace.advanced_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import com.outerspace.advanced_app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.viewModel = viewModel
        binding.progressButton.lifecycleOwner = this
        binding.progressButton.setOnClickListener {
            if(viewModel.selectedUrl != null) {
                FileDownload.Factory()
                    .url(viewModel.selectedUrl!!)
                    .mutableProgress(binding.progressButton.progress)
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
        binding.progressButton.progress.value = 0.0F
    }
}