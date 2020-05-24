package net.kwmt27.detectfacewithcamerax.ui.main.view.textrecoginition

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import com.google.firebase.ml.vision.text.FirebaseVisionText
import net.kwmt27.detectfacewithcamerax.ui.main.view.GraphicOverlay

/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
class TextGraphic(
    private val overlay: GraphicOverlay,
    private val text: FirebaseVisionText.Element?
) : GraphicOverlay.Graphic(overlay) {

    private val rectPaint = Paint().apply {
        color = TEXT_COLOR
        style = Paint.Style.STROKE
        strokeWidth = STROKE_WIDTH
    }

    private val textPaint = Paint().apply {
        color = TEXT_COLOR
        textSize = TEXT_SIZE
    }
    private val testRectPaint = Paint().apply {
        color = Color.BLUE
    }

    /** Draws the text block annotations for position, size, and raw value on the supplied canvas.  */
    override fun draw(canvas: Canvas) {

//        canvas.drawRect(this.rect, testRectPaint)
        Log.d(TAG, "canvas:${canvas.width},${canvas.height}")

        // 反転しているか確認したいので、テストdraw描く
        canvas.drawRect(Rect(0, 0, 30, 30), testRectPaint)

        text?.let { txt ->
            val scaleX = overlay.scaleX
            val scaleY = overlay.scaleY

            val x = scaleX(scaleX)
            val y = scaleY(scaleY)
            Log.d(TAG, "scaleX: $x, scaleY: $y")

            // Draws the bounding box around the TextBlock.
            val rect = RectF(txt.boundingBox)

//            val tx = translateX(rect.centerX())
//            val ty = translateY(rect.centerY())
//            Log.d(TAG, "translateX: $tx, translateY: $ty")

            rect.left = translateX(rect.left)
            rect.top = translateY(rect.top)
            rect.right = translateX(rect.right)
            rect.bottom = translateY(rect.bottom)
            canvas.drawRect(rect, rectPaint)
            Log.d(
                TAG,
                "boundingBox:${txt.text}:${rect.left},${rect.top},${rect.right},${rect.bottom}"
            )

            // Renders the text at the bottom of the box.
            canvas.drawText(txt.text, rect.left, rect.bottom, textPaint)
        } ?: kotlin.run { throw IllegalStateException("Attempting to draw a null text.") }
    }

    companion object {
        val TAG = TextGraphic::class.java.simpleName

        private const val TEXT_COLOR = Color.RED
        private const val TEXT_SIZE = 70.0f
        private const val STROKE_WIDTH = 4.0f
    }
}
