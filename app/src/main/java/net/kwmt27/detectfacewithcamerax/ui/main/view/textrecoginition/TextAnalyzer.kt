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
import net.kwmt27.detectfacewithcamerax.ui.main.view.FrameMetadata
import net.kwmt27.detectfacewithcamerax.ui.main.view.GraphicOverlay
import net.kwmt27.detectfacewithcamerax.ui.main.view.face.FaceAnalyzer
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class TextAnalyzerResult(
    val visionText: FirebaseVisionText,
    val frameMetaData: FrameMetadata
)

class TextAnalyzer(private val lensFacing: Int) : ImageAnalysis.Analyzer {

    private val detector: FirebaseVisionTextRecognizer =
        FirebaseVision.getInstance().onDeviceTextRecognizer

    val liveData = MutableLiveData<TextAnalyzerResult>()

    private var isDetecting = AtomicBoolean(false)

    override fun analyze(imageProxy: ImageProxy) {
        if (isDetecting.get()) {
            imageProxy.close()
            return
        }
        isDetecting.set(true)

        Log.d(this.javaClass.simpleName, "imageProxy: ${imageProxy.width}, ${imageProxy.height}")
        val image = imageProxy.image!!
        val rotation = FaceAnalyzer.translateFirebaseRotation(imageProxy.imageInfo.rotationDegrees)

        val visionImage = FirebaseVisionImage.fromMediaImage(image, rotation)

        imageProxy.close()

//        detectTextNormalListener(visionImage) {
//            liveData.postValue(it)
//            isDetecting = false
//        }

        val frameMetaData =
            FrameMetadata.Builder().setWidth(imageProxy.width).setHeight(imageProxy.height)
                .setCameraFacing(lensFacing)
                .setRotation(rotation).build()


        imageProxy.close()
        val scope = CoroutineScope(Dispatchers.Default)
        val job = scope.launch {
            val visionText = detectText(visionImage)
            liveData.postValue(TextAnalyzerResult(visionText, frameMetaData))
            isDetecting.set(false)
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
        Log.d(
            "TextAnalyzer",
            "updateTextUI${result.frameMetaData.width}, ${result.frameMetaData.height}, ${result.frameMetaData.cameraFacing}"
        )

        graphicOverlay.clear()
        graphicOverlay.setCameraInfo(
            result.frameMetaData.height,
            result.frameMetaData.width,
            result.frameMetaData.cameraFacing
        )
        Log.d("TextAnalyzer", "$result")
        val blocks = result.visionText.textBlocks
        blocks.forEach { block ->
            val lines = block.lines
            lines.forEach { line ->
                val elements = line.elements
                elements.forEach { element ->
                    val textGraphic = TextGraphic(graphicOverlay, element)
                    val box = element.boundingBox ?: return@forEach
                    Log.d(
                        "TextAnalyzer",
                        "${element.text}:boundingBox:${box.left}, ${box.top}, ${box.right}, ${box.bottom}, ${box.width()}, ${box.height()}"
                    )
                    graphicOverlay.add(textGraphic)
                }
            }
        }
        graphicOverlay.postInvalidate()
    }

//    private fun detectTextNormalListener(
//        image: FirebaseVisionImage,
//        callback: (TextAnalyzerResult) -> Unit
//    ) {
//        detector.processImage(image)
//            .addOnSuccessListener { results ->
//                callback(TextAnalyzerResult(results))
//            }
//    }
}
