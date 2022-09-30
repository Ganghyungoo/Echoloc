package com.example.echoloc

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.echoloc.database.Pref

class SplashScreen : AppCompatActivity() {
    lateinit var preferance: Pref
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_splash_screen)
        preferance=Pref(applicationContext)


    Handler().postDelayed(
        {

            if(preferance.getData("name") == "")
            {
                var intent=Intent(applicationContext,Login::class.java)
                startActivity(intent)
                finish()
            }else{
                var intent =Intent(applicationContext,Login::class.java)
                startActivity(intent)
                finish()
                }

            },200
        );
    }
}