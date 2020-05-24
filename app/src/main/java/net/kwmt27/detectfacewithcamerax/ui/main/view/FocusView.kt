package net.kwmt27.detectfacewithcamerax.ui.main.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.view.View

// https://stackoverflow.com/a/47470844
class FocusView : View {
    private val mCutPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mBitmap: Bitmap? = null
    private var mInternalCanvas: Canvas? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init()
    }

    private fun init() {
        mCutPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (mInternalCanvas != null) {
            mInternalCanvas?.setBitmap(null)
            mInternalCanvas = null
        }
        if (mBitmap != null) {
            mBitmap?.recycle()
            mBitmap = null
        }
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mBitmap = bitmap
        mInternalCanvas = Canvas(bitmap)
    }

    override fun onDraw(canvas: Canvas) {
        if (mInternalCanvas == null || mBitmap == null) {
            return
        }
        val bitmap: Bitmap = mBitmap!!
        val width: Int = width
        val height: Int = height

        // make the radius as large as possible within the view bounds
        val radius = Math.min(width, height) / 2
        mInternalCanvas?.drawColor(-0xff01)
        mInternalCanvas?.drawCircle(
            (width / 2).toFloat(), (height / 2).toFloat(),
            radius.toFloat(), mCutPaint
        )
        canvas.drawBitmap(bitmap, 0f, 0f, null)
    }
}