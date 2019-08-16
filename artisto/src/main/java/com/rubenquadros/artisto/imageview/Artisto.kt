package com.rubenquadros.artisto.imageview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.widget.ImageView
import com.rubenquadros.artisto.R
import com.rubenquadros.artisto.callbacks.IAsync
import com.rubenquadros.artisto.service.BackgroundTask

@Suppress("SpellCheckingInspection")
class Artisto(context: Context?, attrs: AttributeSet?, defStyle: Int) : ImageView(context, attrs, defStyle), IAsync {

    init {
        init(attrs)
    }

    private var mImageURL: String? = null
    private var mPlaceholder: String? = ""
    private lateinit var mDrawable: Drawable
    private var mResourceId: Int = 0
    private var mBitmap: Bitmap? = null
    private lateinit var backgroundTask: BackgroundTask
    private val bitmapResource: Bitmap?
        get() {
            if (mBitmap == null) {
                val drawable = drawable ?: return null

                return if (width == 0 || height == 0) {
                    null
                } else (drawable as BitmapDrawable).bitmap

            } else {
                return mBitmap
            }
        }

    constructor(context: Context?): this(context, null, 0) {
        ImageView(context)
    }

    constructor(context: Context?, attrs: AttributeSet?): this(context, attrs, 0) {
        ImageView(context, attrs)
    }

    private fun init(set: AttributeSet?) {
        if(set == null){
            return
        }
        val typedArray = context.obtainStyledAttributes(set, R.styleable.Artisto)
        mPlaceholder = typedArray.getString(R.styleable.Artisto_placeholder)
        mImageURL = typedArray.getString(R.styleable.Artisto_imageURL)
        typedArray.recycle()

        try {
            if(mPlaceholder.isNullOrEmpty() || mPlaceholder.equals("")) {
                mDrawable = ContextCompat.getDrawable(context, android.R.drawable.progress_indeterminate_horizontal)!!
                setImageDrawable(mDrawable)
            }else{
                mResourceId = context.resources.getIdentifier(mPlaceholder, "drawable", context.packageName)
                setImageResource(mResourceId)
            }
        }catch (e: Exception) {
            e.printStackTrace()
        }
        if(mImageURL != null) {
            getBitmap(mImageURL!!)
        }
    }

    private fun setupBitmap(bitmap: Bitmap?) {
        if(bitmap != null) {
            setImageBitmap(bitmap)
        }
    }

    private fun getBitmap(imgUrl: String) {
        backgroundTask = BackgroundTask()
        backgroundTask.setListener(this)
        backgroundTask.execute(imgUrl)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        backgroundTask.cancel(true)
    }

    fun imageURL(mImgUrl: String?) {
        if(mImgUrl != null) {
            getBitmap(mImgUrl)
        }
    }

    override fun taskOnComplete(bitmap: Bitmap?) {
        mBitmap = bitmap
        setupBitmap(mBitmap)
    }
}