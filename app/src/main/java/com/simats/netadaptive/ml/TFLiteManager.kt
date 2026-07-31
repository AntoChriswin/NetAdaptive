package com.simats.netadaptive.ml

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
// import org.tensorflow.lite.flex.FlexDelegate
import org.tensorflow.lite.support.common.FileUtil

/**
 * Manages the TFLite interpreter and model loading with Flex support.
 */
class TFLiteManager(private val context: Context) {
    private val tag = "TFLiteManager"
    private var interpreter: Interpreter? = null
    private val modelPath = "network_model.tflite"

    init {
        initializeInterpreter()
    }

    /**
     * Initializes the TFLite interpreter using CPU with Flex Delegate.
     */
    private fun initializeInterpreter() {
        try {
            // Load model from assets using FileUtil
            val modelBuffer = FileUtil.loadMappedFile(context, modelPath)
            
            // Options without FlexDelegate (FlexDelegate removed as per new requirements)
            val options = Interpreter.Options().apply {
                setNumThreads(4)
                // addDelegate(FlexDelegate())
            }
            
            interpreter = Interpreter(modelBuffer, options)
            Log.d(tag, "TFLite Interpreter initialized successfully with FlexDelegate")
            
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize TFLite interpreter: ${e.message}", e)
            interpreter = null
        }
    }

    /**
     * Thread-safe access to the interpreter.
     */
    fun getInterpreter(): Interpreter? {
        synchronized(this) {
            if (interpreter == null) {
                initializeInterpreter()
            }
            return interpreter
        }
    }

    /**
     * Closes resources.
     */
    fun close() {
        synchronized(this) {
            interpreter?.close()
            interpreter = null
        }
    }
}
