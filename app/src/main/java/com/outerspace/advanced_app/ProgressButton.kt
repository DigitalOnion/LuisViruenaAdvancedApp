package com.outerspace.advanced_app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer


// reference: https://developer.android.com/develop/ui/views/layout/custom-views/create-view
class ProgressButton(context: Context, attributes: AttributeSet? = null): View(context, attributes) {
    private var buttonWidth: Float = 0.0F
    private var buttonHeight: Float = 0.0F
    private val fillPaint = Paint()
    private val emptyPaint = Paint()
    val progress by lazy {
        MutableLiveData<Float>(0.0F)
    }

    var lifecycleOwner: LifecycleOwner? = null
        set(parent) {
        field = parent
        if (field != null)
            progress.observe(field!!) { updateProgress ->
                run {
                    this.invalidate()
                }
            }
    }

    init {
        fillPaint.color = Color.RED
        emptyPaint.color = Color.TRANSPARENT
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        buttonWidth = w.toFloat()
        buttonHeight = h.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0.0F ,0.0F , buttonWidth * progress.value!!, buttonHeight, fillPaint)
        canvas.drawRect(buttonWidth * progress.value!!, 0.0F, buttonWidth, buttonHeight, emptyPaint)
    }
}