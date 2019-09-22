package com.rubenquadros.artisto.callbacks

import android.graphics.Bitmap

/*
* Callback that delivers the image on server to Artisto
*/
interface IImageAsync {
    fun decodedImage(bitmap: Bitmap?)
}