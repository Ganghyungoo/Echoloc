package com.example.echoloc

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.echoloc.database.Pref
import com.example.echoloc.model.Usermodel
import com.example.echoloc.util.showToast
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_login.*

class Login : AppCompatActivity(),View.OnClickListener {
    lateinit var database: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var preferance:Pref
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        preferance= Pref(applicationContext)
        btn_createnew.setOnClickListener(this)
        login_btn.setOnClickListener(this)
        database=FirebaseDatabase.getInstance()
        databaseReference=database.getReference("Echoloc").child("Users")

    }

    override fun onClick(p0: View?) {
        when(p0){
            btn_createnew -> {
                var intent=Intent(applicationContext,Signup::class.java)
                startActivity(intent)
            }
            login_btn -> {
                var email=login_et_email.text.toString().trim()
                var pass=login_et_pass.text.toString().trim()

                if(email.isEmpty() || pass.isEmpty())
                {
                    showToast(applicationContext, msg = "이메일과 비밀번호를 모두 입력하세요!")
                }else{
                    isEmailExist(email, pass)
                }


            }
        }
    }
    private fun isEmailExist(email:String,pass:String) {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var list=ArrayList<Usermodel>()
                var isemailexist=false

                for(postsnapshot in snapshot.children) {
                    var value=postsnapshot.getValue(Usermodel::class.java)
                    if(value!!.email == email && value!!.pass==pass) {
                        isemailexist=true
                        preferance.saveData("name",value.name)
                        preferance.saveData("id",value.id)
                        preferance.saveData("email",value.email)
                        preferance.saveData("call",value.call)

                    }
                    list.add(value)
                }
                if(isemailexist)
                {

                    showToast(applicationContext, msg = "로그인 성공")
                    startActivity(Intent(applicationContext,MainActivity::class.java))
                    finish()


                }else {
                    showToast(applicationContext, msg = "로그인을 실패했습니다. 이메일과 패스워드를 확인하세요")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }
}

//dsadsa