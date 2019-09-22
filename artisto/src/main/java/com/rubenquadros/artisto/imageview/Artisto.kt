@file:Suppress("DEPRECATION")

package com.rubenquadros.artisto.imageview

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Movie
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.SystemClock
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.widget.ImageView
import com.rubenquadros.artisto.R
import com.rubenquadros.artisto.callbacks.IGifAsync
import com.rubenquadros.artisto.callbacks.IImageAsync
import com.rubenquadros.artisto.service.BackgroundTask
import com.rubenquadros.artisto.utility.ApplicationConstants
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.InputStream
import java.nio.ByteBuffer
import kotlin.math.min

@Suppress("SpellCheckingInspection")
class Artisto(context: Context?, attrs: AttributeSet?, defStyle: Int) : ImageView(context, attrs, defStyle), IImageAsync, IGifAsync {

    /*
    * Properties of Artisto
    */
    private var mImageURL: String? = null
    private var mPlaceholder: String? = ""
    private var mGifName: String? = null
    private var mGifURL: String? = null
    private var mType: String? = null

    private var gifDrawableOffline: Drawable? = null
    private var gifDrawableOnline: Drawable? = null
    private lateinit var mDrawable: Drawable
    private var mResourceId: Int = 0
    private var mBitmap: Bitmap? = null
    private var getOnlineImage: BackgroundTask.GetOnlineImage? = null
    private var getOnlineGif: BackgroundTask.GetOnlineGif? = null
    private var mInputStream: InputStream? = null
    private var mMovie: Movie? = null
    private var mMeasuredMovieWidth: Int = 0
    private var mMeasuredMovieHeight: Int = 0
    private var mScaleW = 1f
    private var mScaleH = 1f
    private var mLeft = 0f
    private var mTop = 0f
    private lateinit var assetManager: AssetManager
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private var start: Long = 0

    init {
        initView(attrs)
    }

    constructor(context: Context?): this(context, null, 0) {
        ImageView(context)
    }

    constructor(context: Context?, attrs: AttributeSet?): this(context, attrs, 0) {
        ImageView(context, attrs)
    }

    private fun initView(set: AttributeSet?) {
        if(set == null){
            return
        }
        val typedArray = context.obtainStyledAttributes(set, R.styleable.Artisto)
        mPlaceholder = typedArray.getString(R.styleable.Artisto_placeholder)
        mImageURL = typedArray.getString(R.styleable.Artisto_imageURL)
        mGifName = typedArray.getString(R.styleable.Artisto_gif)
        mGifURL = typedArray.getString(R.styleable.Artisto_gifURL)
        mType = typedArray.getString(R.styleable.Artisto_contentMode)
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
        if(mGifName != null) {
            setGifImageResource(mGifName)
        }
        if(mGifURL != null) {
            getGif(mGifURL!!)
        }
    }

    private fun setupBitmap(bitmap: Bitmap?) {
        if(bitmap != null) {
            setImageBitmap(bitmap)
        }
    }

    private fun getBitmap(imgUrl: String) {
        getOnlineImage = BackgroundTask.GetOnlineImage()
        getOnlineImage?.setImageListener(this)
        getOnlineImage?.execute(imgUrl)
    }

    private fun getGif(gifURL: String) {
        getOnlineGif = BackgroundTask.GetOnlineGif()
        getOnlineGif?.setGifListener(this)
        getOnlineGif?.execute(gifURL)
    }

    private fun setGifImageResource(name: String?) {
        val assets = context.assets
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            doAsync{
                try {
                    val source = ImageDecoder.createSource(assets, name!!)
                    gifDrawableOffline = ImageDecoder.decodeDrawable(source)
                }catch (e: Exception) {
                    e.printStackTrace()
                    gifDrawableOffline = null
                }
                uiThread {
                    setImageDrawable(gifDrawableOffline)
                    if(gifDrawableOffline != null && gifDrawableOffline is AnimatedImageDrawable) {
                        (gifDrawableOffline as AnimatedImageDrawable).start()
                        (gifDrawableOffline as AnimatedImageDrawable).repeatCount =
                            AnimatedImageDrawable.REPEAT_INFINITE
                    }
                }
            }
        }else {
            try {
                assetManager = context.assets
                doAsync {
                    mInputStream = assetManager.open(name!!)
                    mMovie = Movie.decodeStream(mInputStream)
                    uiThread {
                        setImageDrawable(null)
                        mWidth = mMovie!!.width()
                        mHeight = mMovie!!.height()
                        requestLayout()
                    }
                }
            }catch (e: Exception) {
                setImageDrawable(null)
                e.printStackTrace()
            }
        }
    }

    private fun setOnlineGifImageResource(inputStream: InputStream?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            doAsync {
                try {
                    val bytes = inputStream!!.readBytes()
                    val byteBuffer = ByteBuffer.wrap(bytes)
                    val source = ImageDecoder.createSource(byteBuffer)
                    gifDrawableOnline = ImageDecoder.decodeDrawable(source)
                }catch (e: Exception) {
                    e.printStackTrace()
                    gifDrawableOnline = null
                }
                uiThread {
                    setImageDrawable(gifDrawableOnline)
                    if(gifDrawableOnline != null && gifDrawableOnline is AnimatedImageDrawable) {
                        (gifDrawableOnline as AnimatedImageDrawable).start()
                        (gifDrawableOnline as AnimatedImageDrawable).repeatCount = AnimatedImageDrawable.REPEAT_INFINITE
                    }
                }
            }
        }else {

        }
    }

    private fun finishView() {
        if(getOnlineImage != null) {
            getOnlineImage?.cancel(true)
        }
        if(getOnlineGif != null) {
            getOnlineGif?.cancel(true)
        }
        if(gifDrawableOffline != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                (gifDrawableOffline as AnimatedImageDrawable).stop()
            }
        }
        if(gifDrawableOnline != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                (gifDrawableOnline as AnimatedImageDrawable).stop()
            }
        }
        if(mMovie != null) {
            mInputStream!!.close()
        }
    }

    /*
    * A request that sets the image to Artisto
    */
    fun imageURL(mImgUrl: String?) {
        if(mImgUrl != null) {
            getBitmap(mImgUrl)
        }
    }

    /*
    * A request that sets GIF from server to Artisto
    */
    fun setGifImageURL(gifURL: String) {
        getGif(gifURL)
    }

    /*
    * A request that sets GIF from assets folder to Artisto
    */
    fun setGif(name: String) {
        setGifImageResource(name)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if(mMovie != null) {

            /*
			 * Calculate horizontal scaling
			 */
            val measureModeWidth = MeasureSpec.getMode(widthMeasureSpec)
            var scaleW = 1f
            var scaleH = 1f
            if (measureModeWidth != MeasureSpec.UNSPECIFIED) {
                val maximumWidth = MeasureSpec.getSize(widthMeasureSpec)
                scaleW = if (mWidth > maximumWidth) {
                    mWidth.toFloat() / maximumWidth.toFloat()
                } else {
                    maximumWidth.toFloat() / mWidth.toFloat()
                }
            }

            /*
			 * calculate vertical scaling
			 */
            val measureModeHeight = MeasureSpec.getMode(heightMeasureSpec)

            if (measureModeHeight != MeasureSpec.UNSPECIFIED) {
                val maximumHeight = MeasureSpec.getSize(heightMeasureSpec)
                scaleH = if (mHeight > maximumHeight) {
                    mHeight.toFloat() / maximumHeight.toFloat()
                } else {
                    maximumHeight.toFloat() / mHeight.toFloat()
                }
            }

            /*
			 * calculate overall scale
			 */
            if(mType != null) {
                when (mType) {
                    ApplicationConstants.FIT_CENTER -> {
                        mScaleH = min(scaleW, scaleH)
                        mScaleW = mScaleH
                    }
                    ApplicationConstants.AS_IS -> {
                        mScaleH = 1f
                        mScaleW = 1f
                    }
                    ApplicationConstants.FIT_XY -> {
                        mScaleH = scaleH
                        mScaleW = scaleW
                    }
                    else -> {
                        mScaleH = scaleH
                        mScaleW = scaleW
                    }
                }
            }

            mMeasuredMovieWidth = (mWidth * scaleW).toInt()
            mMeasuredMovieHeight = (mHeight * scaleH).toInt()

            setMeasuredDimension(mMeasuredMovieWidth, mMeasuredMovieHeight)
        }else {
            setMeasuredDimension(suggestedMinimumWidth, suggestedMinimumHeight)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mLeft = (width - mMeasuredMovieWidth)/2f
        mTop = (height - mMeasuredMovieHeight)/2f
    }

    override fun onDraw(canvas: Canvas?) {
        val now = SystemClock.uptimeMillis()
        if(start ==  0L) {
            start = now
        }
        if(mMovie != null) {
            var duration = mMovie?.duration()
            if(duration == 0) {
                duration = 1000
            }
            val relTime = ((now-start) % duration!!)
            mMovie?.setTime(relTime.toInt())
            canvas?.scale(mScaleW, mScaleH)
            mMovie?.draw(canvas, mLeft/mScaleW, mTop/mScaleH)
            invalidate()
        }
        super.onDraw(canvas)
    }

    override fun decodedImage(bitmap: Bitmap?) {
        if(bitmap == null) {
            setImageDrawable(null)
        }else {
            mBitmap = bitmap
            setupBitmap(mBitmap)
        }
    }

    override fun receivedGif(inputStream: InputStream?) {
        if(inputStream == null) {
            setImageDrawable(null)
        }else {
            setOnlineGifImageResource(inputStream)
        }
    }

    override fun onDetachedFromWindow() {
        finishView()
        super.onDetachedFromWindow()
    }
}