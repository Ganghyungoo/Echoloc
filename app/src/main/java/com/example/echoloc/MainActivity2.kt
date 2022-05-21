package com.example.echoloc

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.echoloc.database.Pref
import kotlinx.android.synthetic.main.activity_main2.*

class MainActivity2 : AppCompatActivity(), View.OnClickListener {
    lateinit var pref: Pref
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        pref=Pref(applicationContext)
        btn_logout.setOnClickListener(this)
        btn_createroom.setOnClickListener(this)
    }
//강현구 왔다가감ㅋㅋㅋ
    override fun onClick(p0: View?) {
        when(p0)
        {
            btn_logout ->
            {
                pref.clearData()
                finish()
            }
            btn_createroom -> {
                startActivity(Intent(applicationContext,CreateRoom::class.java))
            }
        }
    }
}