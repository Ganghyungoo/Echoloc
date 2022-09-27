package com.example.echoloc

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import com.example.echoloc.database.Pref
import com.example.echoloc.model.Usermodel
import com.example.echoloc.util.showToast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_signup.*


class Signup : AppCompatActivity(),View.OnClickListener {
    lateinit var database:FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var preferance: Pref
    private var imageUri : Uri? = null
    private var imageUri2: Uri? = null//기본 이미지 변수 할당
    var profileCheck = false //추가
    //이미지 등록
    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if(result.resultCode == RESULT_OK) {
                imageUri = result.data?.data //이미지 경로 원본
                iv_profileImg.setImageURI(imageUri) //이미지 뷰를 바꿈
                Log.d("이미지", "성공")
            }
            else{
                Log.d("이미지", "실패")
            }
        }
    var drawablePath: String = getURLForResource(R.drawable.person)

    private fun getURLForResource(resId: Int): String {
        return Uri.parse("android.resource://" + R::class.java.getPackage().name + "/" + resId).toString()
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        preferance = Pref(applicationContext)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)//추가
        back_button.setOnClickListener(this)
        btn_gotologin.setOnClickListener(this)
        btn_change.setOnClickListener(this)
        val profile = findViewById<ImageView>(R.id.iv_profileImg) //추가
        database=FirebaseDatabase.getInstance()
        databaseReference=database.getReference("Echoloc").child("Users")

        profile.setOnClickListener{
            val intentImage = Intent(Intent.ACTION_PICK)
            intentImage.type = MediaStore.Images.Media.CONTENT_TYPE
            getContent.launch(intentImage)
            profileCheck = true
        }//사진 선택 코드
    }

    override fun onClick(p0: View?) {
        when(p0){
            back_button ->{
                onBackPressed()
            }
            btn_gotologin->{
                onBackPressed()
            }
            btn_change ->
            {
                getData()
            }
        }
    }
    private fun getData()
    {
        var name=et_name.text.toString().trim()
        var email=tv_email.text.toString().trim()
        var pass=et_pass.text.toString().trim()
        var call=et_call.text.toString().trim()
        var con_pass=et_confirmpass.text.toString().trim()
        var userProfile=iv_profileImg.imageAlpha.toString()
        if(name.isEmpty() || email.isEmpty() || pass.isEmpty() || con_pass.isEmpty() || call.isEmpty() ||userProfile.isEmpty())
        {
            showToast(applicationContext, msg = "모든 항목을 입력하세요!")
        }
        else{
            if(pass.equals(con_pass))
            {
                var id=databaseReference.push().key
                if(!profileCheck){//사진 설정 안했을때
                    imageUri2=drawablePath.toUri()
                    FirebaseStorage.getInstance().reference.child("userImages").child("$id/photo").putFile(imageUri2!!)
                        .addOnSuccessListener {
                            var userProfile: Uri? = null
                            FirebaseStorage.getInstance().reference.child("userImages")
                                .child("$id/photo").downloadUrl
                                .addOnSuccessListener {
                                    userProfile = it
                                    Log.d("이미지 URL", "$userProfile")
                                    var model=Usermodel(id!!, name, email, pass, call,
                                        userProfile.toString()
                                    )
                                    databaseReference.child(id!!).setValue(model).addOnCompleteListener {
                                        preferance.saveData("name",name)
                                        preferance.saveData("id",id)
                                        preferance.saveData("email",email)
                                        preferance.saveData("call",call)
                                        preferance.saveData("profile",userProfile.toString())
                                        showToast(applicationContext, msg = "회원가입 완료!")
                                        startActivity(Intent(applicationContext,MainActivity::class.java))
                                        finish()
                                    }
                                }
                        }

                }else {
                    FirebaseStorage.getInstance().reference.child("userImages").child("$id/photo").putFile(imageUri!!)
                        .addOnSuccessListener {
                            var userProfile: Uri? = null
                            FirebaseStorage.getInstance().reference.child("userImages")
                                .child("$id/photo").downloadUrl
                                .addOnSuccessListener {
                                    userProfile = it
                                    Log.d("이미지 URL", "$userProfile")
                                    var model=Usermodel(id!!, name, email, pass, call,
                                        userProfile.toString()
                                    )
                                    databaseReference.child(id!!).setValue(model).addOnCompleteListener {
                                        preferance.saveData("name",name)
                                        preferance.saveData("id",id)
                                        preferance.saveData("email",email)
                                        preferance.saveData("call",call)
                                        preferance.saveData("profile",userProfile.toString())
                                        showToast(applicationContext, msg = "회원가입 완료!")
                                        startActivity(Intent(applicationContext,MainActivity::class.java))
                                        finish()
                                    }
                                }
                        }

                }
            }else{
                showToast(applicationContext, msg = "비밀번호를 확인하세요!")
            }
        }
    }
}