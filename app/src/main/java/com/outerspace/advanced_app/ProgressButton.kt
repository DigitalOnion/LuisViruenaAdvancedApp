package com.outerspace.advanced_app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import java.util.concurrent.ArrayBlockingQueue

const val COUNTER_INTERVAL = 20L
const val COUNTER_DELTA = 0.01F

const val STATUS_STARTED = "started"
const val STATUS_FINISHED = "finished"
const val STATUS_ERROR = "error"

// reference: https://developer.android.com/develop/ui/views/layout/custom-views/create-view
class ProgressButton(context: Context, attributes: AttributeSet? = null):
        View(context, attributes),
        ProgressConsumerInterface {
    private var buttonWidth: Float = 0.0F
    private var buttonHeight: Float = 0.0F
    private val fillPaint = Paint()
    private val emptyPaint = Paint()
    private val anglePaint = Paint()
    private val writePaint = Paint()
    private val bounds = Rect()

    private val mutableProgress = MutableLiveData<Float>()
    private var currentProgress: Float = 0.0F
    private var status = ""
    private val queue = ArrayBlockingQueue<Float>(100)

    private lateinit var job: Job
    private var queueJob: Job? = null

    override fun progress(progress: Float) {
        mutableProgress.value = progress
    }

    override fun start() {
        status = STATUS_STARTED
        mutableProgress.value = 0.0F
    }

    override fun finish() {
        status = STATUS_FINISHED
    }

    override fun error(errorDescriptionRef: Int) {
        status = STATUS_ERROR
        Toast.makeText(context, errorDescriptionRef, Toast.LENGTH_SHORT).show()
    }

    fun reset() {
        currentProgress = 0.0F
        queue.clear()
        invalidate()
    }

    var lifecycleOwner: LifecycleOwner? = null
    set(owner) {
        field = owner
        if (field != null) {
            mutableProgress.observe(field!!) {
                job = owner!!.lifecycleScope.launch {
                    while (currentProgress < mutableProgress.value!!) {
                        currentProgress += COUNTER_DELTA
                        invalidate()
                        delay(COUNTER_INTERVAL)
                    }
                }
            }
        }
    }

    init {
        fillPaint.color = Color.RED
        emptyPaint.color = Color.GREEN
        anglePaint.color = Color.BLACK
        writePaint.color = Color.WHITE
        this.background = AppCompatResources.getDrawable(context, R.drawable.rounded_corner_background)
        this.clipToOutline = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        buttonWidth = w.toFloat()
        buttonHeight = h.toFloat()
        writePaint.textSize = buttonHeight * 0.2F
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val progressVal = (currentProgress * 100F).toInt()
        val buttonText = if (currentProgress > 0.0F && currentProgress < 1.0F) {
            canvas.drawRect(0.0F ,0.0F , buttonWidth * currentProgress, buttonHeight, fillPaint)
            canvas.drawRect(buttonWidth * currentProgress, 0.0F, buttonWidth, buttonHeight, emptyPaint)
            canvas.drawArc((buttonWidth - buttonHeight * 0.8F) / 2.0F, buttonHeight * 0.1F,
                (buttonWidth + buttonHeight * 0.8F) / 2.0F, buttonHeight * 0.9F,
                0.0F, 360F * currentProgress, true, anglePaint)
            "$progressVal %"
        } else {
            canvas.drawRect(0.0F ,0.0F , buttonWidth, buttonHeight, emptyPaint)
            context.getString(R.string.click_to_download)
        }
        writePaint.getTextBounds(buttonText, 0, buttonText.length, bounds)
        canvas.drawText(buttonText,
            (buttonWidth - bounds.width()) / 2.0F, (buttonHeight + bounds.height()) / 2.0F,
            writePaint)
    }
}