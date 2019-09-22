package com.rubenquadros.artisto.callbacks

import java.io.InputStream

/*
* Callback that delivers the GIF on server to Artisto
*/
interface IGifAsync {
    fun receivedGif(inputStream: InputStream?)
}