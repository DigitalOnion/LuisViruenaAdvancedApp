package com.outerspace.advanced_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.outerspace.advanced_app.databinding.ActivityMainBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.viewModel = viewModel
        binding.progressButton.lifecycleOwner = this
        binding.progressButton.setOnClickListener {
            binding.progressButton.reset()
            if(viewModel.selectedUrl != null) {
                FileDownload.Factory()
                    .fileUrl(viewModel.selectedUrl!!)
                    .addProgressConsumer(binding.progressButton)
                    .build()
                    .download(this)
            } else {
//                Toast.makeText(this, R.string.select_a_file, Toast.LENGTH_LONG).show()
                binding.progressButton.reset()
                val progressPoints  = arrayOf( 0.10F, 0.33F, 0.66F, 1.00F, 1.00F,)
                MainScope().launch {
                    progressPoints.forEach {
                        binding.progressButton.progress(it)
                        delay(2000)
                    }
                }
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
}