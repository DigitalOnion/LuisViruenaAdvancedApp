package com.outerspace.advanced_app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import java.util.concurrent.ArrayBlockingQueue

const val COUNTER_INTERVAL = 20L
const val COUNTER_DELTA = 0.01F

const val STATUS_READY = "ready"
const val STATUS_STARTED = "started"
const val STATUS_FINISHED = "finished"
const val STATUS_ERROR = "error"

const val PROGRESS_TAG = "PROGRESS"

// reference: https://developer.android.com/develop/ui/views/layout/custom-views/create-view
class ProgressButton(context: Context, attributes: AttributeSet? = null):
        View(context, attributes),
        ProgressConsumerInterface {

    data class QueueElement (val progress: Float, val status: String)

    private var buttonWidth: Float = 0.0F
    private var buttonHeight: Float = 0.0F
    private val fillPaint = Paint()
    private val emptyPaint = Paint()
    private val anglePaint = Paint()
    private val writePaint = Paint()
    private val bounds = Rect()

//    private val mutableProgress = MutableLiveData<Float>()
    private var currentProgress: Float = 0.0F
    private var whatToDraw: ((canvas: Canvas, progress: Float) -> Unit)? = null
    private val queue = ArrayBlockingQueue<QueueElement>(100)

    private lateinit var job: Job

    init {
        fillPaint.color = Color.RED
        emptyPaint.color = Color.GREEN
        anglePaint.color = Color.BLACK
        writePaint.color = Color.WHITE
        this.background = AppCompatResources.getDrawable(context, R.drawable.rounded_corner_background)
        this.clipToOutline = true
    }

    fun reset() {
        Log.d(PROGRESS_TAG, "reset")
        queue.clear()
        queue.add(QueueElement(0.0F, STATUS_READY))
    }

    override fun start() {
        Log.d(PROGRESS_TAG, "start")
        progressLoop()
        queue.add(QueueElement(0.0F, STATUS_STARTED))
    }

    override fun startAt(progress: Float) {
        Log.d(PROGRESS_TAG, "start at ${(progress * 100).toInt()}")
        progressLoop()
        queue.add(QueueElement(progress, STATUS_STARTED))
    }

    override fun progress(progress: Float) {
        Log.d(PROGRESS_TAG, "progress, progress: ${(progress * 100).toInt()}")
        queue.add(QueueElement(progress, STATUS_STARTED))
    }

    override fun finish() {
        Log.d(PROGRESS_TAG, "finish")
        queue.add(QueueElement(1.0F, STATUS_FINISHED))
    }

    override fun error(errorDescriptionRef: Int) {
        Log.d(PROGRESS_TAG, "error")
        queue.add(QueueElement(0.0F, STATUS_ERROR))
//        Toast.makeText(context, errorDescriptionRef, Toast.LENGTH_SHORT).show()
    }

    var lifecycleOwner: LifecycleOwner? = null

    private fun progressLoop() {
        whatToDraw = drawGreeting
        if (lifecycleOwner != null) {
            job = lifecycleOwner!!.lifecycleScope.launch {
                while (true) {
                    if (queue.isNotEmpty()) {
                        val element = queue.poll()
                        if (element != null) {
                            when (element.status) {
                                STATUS_READY -> {
                                    currentProgress = 0.0F
                                    whatToDraw = drawGreeting
                                    invalidate()
                                }
                                STATUS_STARTED -> {
                                    while (currentProgress <= element.progress) {
                                        currentProgress += COUNTER_DELTA
                                        whatToDraw = drawProgress
                                        invalidate()
                                        delay(COUNTER_INTERVAL)
                                    }
                                }
                                STATUS_FINISHED -> {
                                    whatToDraw = drawProgress
                                    invalidate()
                                    delay(500)
                                    currentProgress = 0.0F
                                    whatToDraw = drawGreeting
                                    invalidate()
                                    job.cancel()
                                    break
                                }
                                STATUS_ERROR -> {
                                    whatToDraw = drawError
                                    invalidate()
                                    job.cancel()
                                    break
                                }
                            }
                        }
                    }
                    delay(COUNTER_INTERVAL)
                }
            }
        }
    }       // start loop

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        buttonWidth = w.toFloat()
        buttonHeight = h.toFloat()
        writePaint.textSize = buttonHeight * 0.2F
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (whatToDraw == null) whatToDraw = drawGreeting
        whatToDraw!!.invoke(canvas, currentProgress)
    }

    private val drawError: (Canvas, Float) -> Unit = {canvas: Canvas, _: Float ->
        canvas.drawRect(0.0F ,0.0F , buttonWidth, buttonHeight, fillPaint)
        drawText(canvas, context.getString(R.string.error))
    }

    private val drawProgress: (Canvas, Float) -> Unit = {canvas: Canvas, progress: Float ->
        val progressVal = (progress * 100F).toInt()
        canvas.drawRect(0.0F ,0.0F , buttonWidth * progress, buttonHeight, fillPaint)
        canvas.drawRect(buttonWidth * progress, 0.0F, buttonWidth, buttonHeight, emptyPaint)
        canvas.drawArc((buttonWidth - buttonHeight * 0.8F) / 2.0F, buttonHeight * 0.1F,
            (buttonWidth + buttonHeight * 0.8F) / 2.0F, buttonHeight * 0.9F,
            0.0F, 360F * progress, true, anglePaint)
        drawText(canvas, "$progressVal %")
    }

    private val drawGreeting: (Canvas, Float) -> Unit = {canvas: Canvas, _: Float ->
        canvas.drawRect(0.0F ,0.0F , buttonWidth, buttonHeight, emptyPaint)
        drawText(canvas, context.getString(R.string.click_to_download))
    }

    private fun drawText(canvas: Canvas, buttonText: String) {
        writePaint.getTextBounds(buttonText, 0, buttonText.length, bounds)
        canvas.drawText(buttonText,
            (buttonWidth - bounds.width()) / 2.0F, (buttonHeight + bounds.height()) / 2.0F,
            writePaint)
    }
}