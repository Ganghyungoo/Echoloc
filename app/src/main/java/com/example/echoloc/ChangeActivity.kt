package com.example.echoloc

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_change.*

lateinit var database: FirebaseDatabase

private lateinit var Profileset: Profilesetting

@Suppress("DEPRECATION")
class ChangeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change)
        bottom_nav.setOnNavigationItemSelectedListener(BottomNavItemSelectedListener)

    }
    private val BottomNavItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener{
        when(it.itemId){
            R.id.menu_profile -> {
                Profileset = Profilesetting.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.fragments_frame, Profileset).commit()
            }
        }
        true
    }
}