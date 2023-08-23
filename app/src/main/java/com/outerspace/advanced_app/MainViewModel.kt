package com.outerspace.advanced_app

import android.content.Context
import android.database.Cursor
import android.util.TypedValue
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

private class RadioButtonSpec(val id: Int, val face: String, val url: String )

private val radioButtonSpecs = arrayOf<RadioButtonSpec>(
    RadioButtonSpec(0, "Glide - Image Loading Library by BumpTech", "https://github.com/bumptech/glide/archive/refs/heads/master.zip"),
    RadioButtonSpec(1, "LoadApp - Current repository by Udacity", "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"),
    RadioButtonSpec(2, "Retrofit - Type-save HTTP Client for Android and Java by Square Inc", "https://github.com/square/retrofit/archive/refs/heads/master.zip"),
)

class MainViewModel: ViewModel() {

    val downloadCompleted: MutableLiveData<Cursor> = MutableLiveData()

    fun addButtons(radioGroup: RadioGroup, context: Context) {
        radioGroup.removeAllViews()
        val txtSize = TypedValue()
        context.resources.getValue(R.dimen.button_text_size, txtSize, true)
        for(spec in radioButtonSpecs) {
            val btn = RadioButton(context)
            btn.textSize = txtSize.float
            btn.setPadding(0,0,0, 40)
            btn.id = spec.id
            btn.text = spec.face
            btn.tag = spec.url
            radioGroup.addView(btn)
        }
    }

    var selectedUrl: String? = null
    lateinit var selectedFileName: String

    val onCheckedListener: RadioGroup.OnCheckedChangeListener =
        RadioGroup.OnCheckedChangeListener { _, checkId ->
            val sel = radioButtonSpecs.find {it.id == checkId}!!
            selectedUrl = sel.url
            selectedFileName = sel.face.replace("[\\s-]+".toRegex(), "-")
        }
}
