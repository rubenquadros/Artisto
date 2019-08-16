package com.rubenquadros.artisto.service

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import com.rubenquadros.artisto.callbacks.IAsync
import java.lang.Exception
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class BackgroundTask: AsyncTask<String, Void, Bitmap>() {

    private var bitmap: Bitmap? = null
    private lateinit var mListener: IAsync
    private lateinit var connection: HttpsURLConnection

    override fun doInBackground(vararg params: String?): Bitmap? {

        val url = URL(params[0])
        return try {
            connection = url.openConnection() as HttpsURLConnection
            connection.doInput = true
            connection.doOutput = false
            connection.connect()
            val inputStream = connection.inputStream
            bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            bitmap
        }catch (e: Exception) {
            null
        }
    }

    override fun onPostExecute(result: Bitmap?) {
        super.onPostExecute(result)
        mListener.taskOnComplete(bitmap)
    }

    fun setListener(listener: IAsync) {
        mListener = listener
    }
}