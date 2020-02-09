package net.kwmt27.detectfacewithcamerax.ui.main.view.face

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.lifecycle.MutableLiveData
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.kwmt27.detectfacewithcamerax.ui.main.view.GraphicOverlay
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class Face(
    val visionFaces: List<FirebaseVisionFace>
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


//        val scope = CoroutineScope(Dispatchers.Default)
//        scope.launch {
//            val visionFaces = detect(visionImage)
//            liveDataFaces.postValue(Face(visionFaces))
//        }

        runBlocking {
            val visionImage = FirebaseVisionImage.fromMediaImage(image, rotation)
            imageProxy.close()

            val visionFaces = detect(visionImage)
            liveDataFaces.postValue(Face(visionFaces))
        }
    }

    suspend fun detect(image: FirebaseVisionImage): List<FirebaseVisionFace> =
        suspendCoroutine { continuation ->
            Log.d("FaceAnalyzer", "detectFace")
            detector.detectInImage(image)
                .addOnSuccessListener { results ->
                    continuation.resume(results)
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }

    fun updateFaceUI(graphicOverlay: GraphicOverlay, face: Face, lensFacing: Int, viewFinder: PreviewView) {
        Log.d("CameraFragment", "update")
        val visionFaces = face.visionFaces
        Log.d("CameraFragment", "faces.isNotEmpty(): ${visionFaces.size}")
        if (visionFaces.isEmpty()) return

        graphicOverlay.clear()

        for (f in visionFaces) {
            Log.d("CameraFragment", "f.boundingBox: ${f.boundingBox}, graphicOverlay: ${graphicOverlay.width}, ${graphicOverlay.height}")
            val faceGraphic = FaceGraphic(graphicOverlay, f, lensFacing, null, viewFinder)
            graphicOverlay.add(faceGraphic)
        }
        graphicOverlay.postInvalidate()
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
