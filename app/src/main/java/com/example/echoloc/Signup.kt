package com.example.echoloc

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.echoloc.database.Pref
import com.example.echoloc.model.Usermodel
import com.example.echoloc.util.showToast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_map.view.*
import kotlinx.android.synthetic.main.activity_signup.*
import java.text.SimpleDateFormat
import java.util.*

class Signup : AppCompatActivity(),View.OnClickListener {
    lateinit var database:FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var preferance: Pref



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        preferance = Pref(applicationContext)
        back_button.setOnClickListener(this)
        btn_gotologin.setOnClickListener(this)
        signup_btn.setOnClickListener(this)
        database=FirebaseDatabase.getInstance()
        databaseReference=database.getReference("Echoloc").child("Users")

    }

    override fun onClick(p0: View?) {
        when(p0){
            back_button ->{
                onBackPressed()
            }
            btn_gotologin->{
                onBackPressed()
            }
            signup_btn ->
            {
                getData()
            }
        }
    }
    private fun getData()
    {
        var name=et_name.text.toString().trim()
        var email=et_email.text.toString().trim()
        var pass=et_pass.text.toString().trim()
        var call=et_call.text.toString().trim()
        var con_pass=et_confirmpass.text.toString().trim()
        if(name.isEmpty() || email.isEmpty() || pass.isEmpty() || con_pass.isEmpty() || call.isEmpty())
        {
            showToast(applicationContext, msg = "모든 항목을 입력하세요!")
        }else{

            if(pass.equals(con_pass))
            {
                var id=databaseReference.push().key
                var model=Usermodel(id!!, name, email, pass, call)
                databaseReference.child(id!!).setValue(model).addOnCompleteListener {

                    preferance.saveData("name",name)
                    preferance.saveData("id",id)
                    preferance.saveData("email",email)
                    preferance.saveData("call",call)
                    showToast(applicationContext, msg = "회원가입 완료!")
                    startActivity(Intent(applicationContext,MainActivity::class.java))
                    finish()
                }
            }else{
                showToast(applicationContext, msg = "비밀번호를 확인하세요!")
            }

        }
    }

}