package net.kwmt27.detectfacewithcamerax.ui.main

import android.graphics.Matrix
import android.media.Image
import android.util.Log
import android.util.Size
import android.view.View
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.MutableLiveData
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class Face(
    val visionFaces: FirebaseVisionImage,
    val imageSize: Size
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
        val imageSize = Size(image.width, image.height)
        imageProxy.close()
        liveDataFaces.postValue(Face(visionImage, imageSize))
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
