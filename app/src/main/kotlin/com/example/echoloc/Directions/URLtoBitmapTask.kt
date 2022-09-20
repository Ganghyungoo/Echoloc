package com.example.echoloc.Directions

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import kotlinx.coroutines.*
import java.net.URL

class URLtoBitmapTask() {

    fun URLtoBitmap(url: URL): Job {
        var bitmap: Bitmap

        return CoroutineScope(Dispatchers.Main).launch {
            bitmap = withContext(Dispatchers.IO) {
                BitmapFactory.decodeStream(url.openStream())
            }
        }
    }
}