package com.rubenquadros.artisto.service

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import com.rubenquadros.artisto.callbacks.IGifAsync
import com.rubenquadros.artisto.callbacks.IImageAsync
import java.io.InputStream
import java.lang.Exception
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/*
* A request that loads resources from server
*/
class BackgroundTask {

    class GetOnlineImage: AsyncTask<String, Void, Bitmap>() {

        private var bitmap: Bitmap? = null
        private lateinit var imageListener: IImageAsync
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
            imageListener.decodedImage(result)
        }

        fun setImageListener(listener: IImageAsync) {
            imageListener = listener
        }
    }

    class GetOnlineGif: AsyncTask<String, Void, InputStream>() {

        private lateinit var gifListener: IGifAsync
        private lateinit var connection: HttpsURLConnection

        override fun doInBackground(vararg params: String?): InputStream? {
            val url = URL(params[0])
            return try {
                connection = url.openConnection() as HttpsURLConnection
                connection.doInput = true
                connection.doOutput = false
                connection.connect()
                val inputStream = connection.inputStream
                inputStream
            }catch (e: Exception) {
                null
            }
        }

        override fun onPostExecute(result: InputStream?) {
            super.onPostExecute(result)
            gifListener.receivedGif(result)
        }

        fun setGifListener(listener: IGifAsync) {
            gifListener = listener
        }
    }
}