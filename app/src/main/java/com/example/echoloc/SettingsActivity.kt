package com.example.echoloc

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.example.echoloc.database.Pref
import com.example.echoloc.model.Usermodel
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var pref: Pref
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        btn_changeProfile.setOnClickListener(this)
        tv_logOut.setOnClickListener(this)
        btn_back.setOnClickListener(this)

        pref = Pref(applicationContext)
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.getReference("Echoloc").child("Users").child(pref.getData("id"))
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userModel = snapshot.getValue(Usermodel::class.java)
                val uri = Uri.parse(userModel!!.profileImageUrl)
                Glide.with(applicationContext).load(uri).into(iv_profile)
                iv_profile.clipToOutline = true
                tv_profileName.text = pref.getData("name")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    override fun onClick(p0: View?) {
        when(p0) {
            btn_changeProfile -> {
                // 프로필 입력하는 액티비티로 이동
                val intent = Intent(applicationContext, ProfileChangeActivity::class.java)
                startActivity(intent)
            }

            tv_logOut -> {
                // 로그아웃
                pref.clearData()
                startActivity(Intent(applicationContext, Login::class.java))
                finish()
            }

            btn_back -> {
                onBackPressed()
            }
        }
    }
}