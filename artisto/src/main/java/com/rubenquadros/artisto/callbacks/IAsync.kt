package com.rubenquadros.artisto.callbacks

import android.graphics.Bitmap

interface IAsync {
    fun taskOnComplete(bitmap: Bitmap?)
}