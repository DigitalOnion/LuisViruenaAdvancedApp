package com.outerspace.advanced_app

import android.content.Context
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.databinding.adapters.TextViewBindingAdapter.setText
import androidx.lifecycle.ViewModel

private class RadioButtonSpec(val id: Int, val face: String, val url: String )

private val radioButtonSpecs = arrayOf<RadioButtonSpec>(
    RadioButtonSpec(0, "Glide - Image Loading Library by BumpTech", "https://github.com/bumptech/glide/archive/refs/heads/master.zip"),
    RadioButtonSpec(1, "LoadApp - Current repository by Udacity", "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"),
    RadioButtonSpec(2, "Retrofit - Type-save HTTP Client for Android and Java by Square Inc", "https://github.com/square/retrofit/archive/refs/heads/master.zip"),
)

class MainViewModel: ViewModel() {

    fun addButtons(radioGroup: RadioGroup, context: Context) {
        radioGroup.removeAllViews()
        for(spec in radioButtonSpecs) {
            val btn = RadioButton(context)
            btn.id = spec.id
            btn.text = spec.face
            btn.tag = spec.url
            radioGroup.addView(btn)
        }
    }

    var selectedUrl: String? = null

    val onCheckedListener: RadioGroup.OnCheckedChangeListener =
        RadioGroup.OnCheckedChangeListener { _, checkId -> selectedUrl = radioButtonSpecs.find {it.id == checkId}!!.url }

    fun testFunction(): String {
        return "Hello"
    }
}