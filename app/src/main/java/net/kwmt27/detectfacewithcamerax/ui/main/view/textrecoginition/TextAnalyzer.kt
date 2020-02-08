package net.kwmt27.detectfacewithcamerax.ui.main.view.textrecoginition

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.MutableLiveData
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer
import net.kwmt27.detectfacewithcamerax.ui.main.view.face.FaceAnalyzer
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class TextAnalyzerResult (
    val visionText: FirebaseVisionImage
)
class TextAnalyzer : ImageAnalysis.Analyzer {

    private val detector: FirebaseVisionTextRecognizer = FirebaseVision.getInstance().onDeviceTextRecognizer

    private val liveData = MutableLiveData<TextAnalyzerResult>()

    override fun analyze(imageProxy: ImageProxy) {
        val image = imageProxy.image!!
        val rotation = FaceAnalyzer.translateFirebaseRotation(imageProxy.imageInfo.rotationDegrees)

        val visionImage = FirebaseVisionImage.fromMediaImage(image, rotation)
        liveData.postValue(TextAnalyzerResult(visionImage))

    }

    suspend fun detectText(image: FirebaseVisionImage): FirebaseVisionText =
        suspendCoroutine { continuation ->
            Log.d("FaceAnalyzer", "detectFace")
            detector.processImage(image)
                .addOnSuccessListener { results ->
                    continuation.resume(results)
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
}
