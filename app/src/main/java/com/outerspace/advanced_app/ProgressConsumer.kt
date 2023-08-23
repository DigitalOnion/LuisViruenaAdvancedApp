package com.outerspace.advanced_app

interface ProgressConsumerInterface {
    fun start()
    fun startAt(progress: Float)
    fun progress(progress: Float)
    fun finish()
    fun error(errorDescriptionRef: Int)
}
