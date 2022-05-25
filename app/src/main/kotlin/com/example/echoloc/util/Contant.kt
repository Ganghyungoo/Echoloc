package com.example.echoloc.util

import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

public fun showToast(context: Context, msg:String) {
    Toast.makeText(context,msg, Toast.LENGTH_LONG).show()
}

public fun getDateTime(): String {
    var sdf = SimpleDateFormat("dd/MM/yy hh:mm:ss")
    var currentdate = sdf.format(Date())
    return currentdate
}