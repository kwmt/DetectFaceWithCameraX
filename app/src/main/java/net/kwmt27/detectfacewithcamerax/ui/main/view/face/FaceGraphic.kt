// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package net.kwmt27.detectfacewithcamerax.ui.main.view.face

import android.graphics.*
import android.util.Log
import androidx.camera.view.PreviewView
import com.google.android.gms.vision.CameraSource
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark
import net.kwmt27.detectfacewithcamerax.ui.main.view.GraphicOverlay
import net.kwmt27.detectfacewithcamerax.ui.main.view.GraphicOverlay.Graphic

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class FaceGraphic(
    overlay: GraphicOverlay,
    private val firebaseVisionFace: FirebaseVisionFace,
    private val facing: Int,
    private val overlayBitmap: Bitmap?
) : Graphic(overlay) {
    private val facePositionPaint: Paint
    private val idPaint: Paint
    private val boxPaint: Paint

    init {
        val selectedColor = Color.WHITE
        facePositionPaint = Paint()
        facePositionPaint.color = selectedColor
        idPaint = Paint()
        idPaint.color = selectedColor
        idPaint.textSize = ID_TEXT_SIZE
        boxPaint = Paint()
        boxPaint.color = selectedColor
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = BOX_STROKE_WIDTH
//        val w = preview.width.toFloat()
//        val h = preview.height.toFloat()
//        val sx = overlay.width.toFloat() / w
//        val sy = overlay.height.toFloat() / h
//        val scale = Math.max(sx, sy)
//        scaleX(sx)
//        scaleY(sy)
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    override fun draw(canvas: Canvas) {
        val face = firebaseVisionFace
        // Draws a circle at the position of the detected face, with the face's track id below.
        // An offset is used on the Y axis in order to draw the circle, face id and happiness level in the top area
        // of the face's bounding box
        val x = translateX(face.boundingBox.centerX().toFloat())
        val y = translateY(face.boundingBox.centerY().toFloat())

        Log.d(
            "FaceGraphic",
            "(x,y)=($x, $y), (centerX, centerY)=(${face.boundingBox.centerX()}, ${face.boundingBox.centerY()}),left= ${face.boundingBox.left}, right=${face.boundingBox.top} "
        )


        canvas.drawCircle(
            x,
            y - 4 * ID_Y_OFFSET,
            FACE_POSITION_RADIUS,
            facePositionPaint
        )
        canvas.drawText(
            "id: " + face.trackingId,
            x + ID_X_OFFSET,
            y - 3 * ID_Y_OFFSET,
            idPaint
        )
        canvas.drawText(
            "happiness: " + String.format("%.2f", face.smilingProbability),
            x + ID_X_OFFSET * 3,
            y - 2 * ID_Y_OFFSET,
            idPaint
        )

        if (facing == CameraSource.CAMERA_FACING_FRONT) {
            canvas.drawText(
                "right eye: " + String.format("%.2f", face.getRightEyeOpenProbability()),
                x - ID_X_OFFSET,
                y,
                idPaint
            );
            canvas.drawText(
                "left eye: " + String.format("%.2f", face.getLeftEyeOpenProbability()),
                x + ID_X_OFFSET * 6,
                y,
                idPaint
            );
        } else {
            canvas.drawText(
                "left eye: " + String.format("%.2f", face.getLeftEyeOpenProbability()),
                x - ID_X_OFFSET,
                y,
                idPaint
            );
            canvas.drawText(
                "right eye: " + String.format("%.2f", face.getRightEyeOpenProbability()),
                x + ID_X_OFFSET * 6,
                y,
                idPaint
            );
        }
// Draws a bounding box around the face.
        val xOffset = scaleX(face.boundingBox.width() / 2.0f)
        val yOffset = scaleY(face.boundingBox.height() / 2.0f)
        val left = x - xOffset
        val top = y - yOffset
        val right = x + xOffset
        val bottom = y + yOffset
        canvas.drawRect(left, top, right, bottom, boxPaint)
        // draw landmarks
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.MOUTH_BOTTOM)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.LEFT_CHEEK)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.LEFT_EAR)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.MOUTH_LEFT)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.LEFT_EYE)
        drawBitmapOverLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.NOSE_BASE)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.RIGHT_CHEEK)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.RIGHT_EAR)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.RIGHT_EYE)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.MOUTH_RIGHT)
    }

    private fun drawLandmarkPosition(
        canvas: Canvas,
        face: FirebaseVisionFace,
        landmarkID: Int
    ) {
        val landmark = face.getLandmark(landmarkID)
        if (landmark != null) {
            val point = landmark.position
            canvas.drawCircle(
                translateX(point.x),
                translateY(point.y),
                10f, idPaint
            )
        }
    }

    private fun drawBitmapOverLandmarkPosition(
        canvas: Canvas,
        face: FirebaseVisionFace,
        landmarkID: Int
    ) {
        val landmark = face.getLandmark(landmarkID) ?: return
        val point = landmark.position
        if (overlayBitmap != null) {
            val imageEdgeSizeBasedOnFaceSize = face.boundingBox.width() / 4.0f
            val left = (translateX(point.x) - imageEdgeSizeBasedOnFaceSize).toInt()
            val top = (translateY(point.y) - imageEdgeSizeBasedOnFaceSize).toInt()
            val right = (translateX(point.x) + imageEdgeSizeBasedOnFaceSize).toInt()
            val bottom = (translateY(point.y) + imageEdgeSizeBasedOnFaceSize).toInt()
            canvas.drawBitmap(
                overlayBitmap,
                null,
                Rect(left, top, right, bottom),
                null
            )
        }
    }

    companion object {
        private const val FACE_POSITION_RADIUS = 4.0f
        private const val ID_TEXT_SIZE = 30.0f
        private const val ID_Y_OFFSET = 50.0f
        private const val ID_X_OFFSET = -50.0f
        private const val BOX_STROKE_WIDTH = 5.0f
    }
}
