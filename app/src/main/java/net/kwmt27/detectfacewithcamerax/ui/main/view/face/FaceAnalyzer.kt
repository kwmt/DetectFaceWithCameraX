package net.kwmt27.detectfacewithcamerax.ui.main.view.face

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.MutableLiveData
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class Face(
    val visionFaces: FirebaseVisionImage
)

class FaceAnalyzer : ImageAnalysis.Analyzer {

    // Real-time contour detection of multiple faces
    private val options by lazy {
        FirebaseVisionFaceDetectorOptions.Builder()
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
            .build()
    }
    private val detector by lazy { FirebaseVision.getInstance().getVisionFaceDetector(options) }

    val liveDataFaces = MutableLiveData<Face>()
    override fun analyze(imageProxy: ImageProxy) {
        Log.d("FaceAnalyzer", "analyze")
        val image = imageProxy.image!!
        val rotation = translateFirebaseRotation(imageProxy.imageInfo.rotationDegrees)

        val visionImage = FirebaseVisionImage.fromMediaImage(image, rotation)
        imageProxy.close()
        liveDataFaces.postValue(Face(visionImage))
    }

    suspend fun detectFace(image: FirebaseVisionImage): List<FirebaseVisionFace> =
        suspendCoroutine { continuation ->
            Log.d("FaceAnalyzer", "detectFace")
            detector.detectInImage(image)
                .addOnSuccessListener { results ->
                    continuation.resume(results)
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }

    companion object {
        fun translateFirebaseRotation(rotationDegrees: Int): Int {
            return when (rotationDegrees) {
                0 -> FirebaseVisionImageMetadata.ROTATION_0
                90 -> FirebaseVisionImageMetadata.ROTATION_90
                180 -> FirebaseVisionImageMetadata.ROTATION_180
                270 -> FirebaseVisionImageMetadata.ROTATION_270
                else -> FirebaseVisionImageMetadata.ROTATION_0
            }
        }
    }
}
