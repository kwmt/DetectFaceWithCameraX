package net.kwmt27.detectfacewithcamerax.ui.main.view.textrecoginition

import android.util.Log
import androidx.annotation.MainThread
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.MutableLiveData
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.kwmt27.detectfacewithcamerax.ui.main.view.GraphicOverlay
import net.kwmt27.detectfacewithcamerax.ui.main.view.face.FaceAnalyzer
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class TextAnalyzerResult(
    val visionText: FirebaseVisionText

)

class TextAnalyzer : ImageAnalysis.Analyzer {

    private val detector: FirebaseVisionTextRecognizer =
        FirebaseVision.getInstance().onDeviceTextRecognizer

    val liveData = MutableLiveData<TextAnalyzerResult>()

    private var isDetecting = false

    override fun analyze(imageProxy: ImageProxy) {
        if(isDetecting) {
            imageProxy.close()
            return
        }
        isDetecting = true
        val image = imageProxy.image!!
        val rotation = FaceAnalyzer.translateFirebaseRotation(imageProxy.imageInfo.rotationDegrees)

        val visionImage = FirebaseVisionImage.fromMediaImage(image, rotation)
        imageProxy.close()

//        detectTextNormalListener(visionImage) {
//            liveData.postValue(it)
//            isDetecting = false
//        }


        imageProxy.close()
        val scope = CoroutineScope(Dispatchers.Default)
        val job = scope.launch {
            val visionText = detectText(visionImage)
            liveData.postValue(TextAnalyzerResult(visionText))
            isDetecting = false
        }
    }

    private suspend fun detectText(image: FirebaseVisionImage): FirebaseVisionText =
        suspendCoroutine { continuation ->
            Log.d("TextAnalyzer", "detectText")
            detector.processImage(image)
                .addOnSuccessListener { results ->
                    continuation.resume(results)
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }

    @MainThread
    fun updateTextUI(graphicOverlay: GraphicOverlay, result: TextAnalyzerResult) {
        Log.d("TextAnalyzer", "updateTextUI")

        graphicOverlay.clear()
        Log.d("TextAnalyzer", "$result")
        val blocks = result.visionText.textBlocks
        blocks.forEach { block ->
            val lines = block.lines
            lines.forEach { line ->
                val elements = line.elements
                elements.forEach { element ->
                    val textGraphic = TextGraphic(graphicOverlay, element)
                    graphicOverlay.add(textGraphic)
                }
            }
        }
        graphicOverlay.postInvalidate()
    }

    private fun detectTextNormalListener(
        image: FirebaseVisionImage,
        callback: (TextAnalyzerResult) -> Unit
    ) {
        detector.processImage(image)
            .addOnSuccessListener { results ->
                callback(TextAnalyzerResult(results))
            }
    }
}
