package com.outerspace.advanced_app

interface ProgressConsumerInterface {
    fun start()
    fun progress(progress: Float)
    fun finish()
    fun error(errorDescriptionRef: Int)
}
