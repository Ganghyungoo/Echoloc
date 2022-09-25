package com.example.echoloc

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.echoloc.database.Pref
import com.example.echoloc.model.Usermodel
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_profile_change.*

class ProfileChangeActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var pref: Pref
    lateinit var database: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    private var imageUri: Uri? = null
    private lateinit var userModel: Usermodel
    private lateinit var editedUserModel: Usermodel
    private lateinit var editedName: String
    private lateinit var editedPass: String
    private var editedImage: Uri? = null
    var flag = true

    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if(result.resultCode == RESULT_OK) {
                editedImage = result.data?.data //이미지 경로 원본
                iv_profileImg.setImageURI(editedImage) //이미지 뷰를 바꿈
                Log.d("이미지", "성공")
            }
            else{
                Log.d("이미지", "실패")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_change)

        back_button.setOnClickListener(this)
        iv_profileImg.setOnClickListener(this)
        btn_change.setOnClickListener(this)

        pref = Pref(applicationContext)
        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("Echoloc").child("Users").child(pref.getData("id"))

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userModel = snapshot.getValue(Usermodel::class.java)!!
                imageUri = Uri.parse(userModel.profileImageUrl)
                Glide.with(this@ProfileChangeActivity).load(imageUri).into(iv_profileImg)
                iv_profileImg.clipToOutline = true
                tv_email.text = userModel.email
                et_name.hint = userModel.name
                et_pass.hint = userModel.pass
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onClick(p0: View?) {
        when(p0) {
            back_button -> {
                onBackPressed()
            }
            // 프로필 사진 선택
            iv_profileImg -> {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = MediaStore.Images.Media.CONTENT_TYPE
                getContent.launch(intent)
            }
            // 프로필 수정
            btn_change -> {
                if (et_name.text.toString().isEmpty()) {
                    editedName = userModel.name
                } else {
                    editedName = et_name.text.toString()
                }

                if (et_pass.text.toString().isEmpty()) {
                    editedPass = userModel.pass
                    flag = true
                } else {
                    if (et_pass.text.toString().equals(et_confirmpass.text.toString())) {
                        editedPass = et_pass.text.toString()
                        flag = true
                    } else {
                        flag = false
                    }
                }

                if (flag) {
                    if (editedImage == null) {
                        editedUserModel = Usermodel(
                            userModel.id, editedName, userModel.email, editedPass, userModel.call, imageUri.toString())
                        databaseReference.setValue(editedUserModel).addOnSuccessListener {
                            pref.saveData("name", editedName)
                            pref.saveData("profile", imageUri.toString())
                            Toast.makeText(this, "프로필 수정이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                            val intent = Intent(applicationContext, SettingsActivity::class.java)
                            startActivity(intent)
                            finish()
                        }.addOnFailureListener {
                            Toast.makeText(this, "프로필 수정이 실패하였습니다.\n", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        FirebaseStorage.getInstance().reference.child("userImages").child("${userModel.id}/photo").putFile(imageUri!!)
                            .addOnSuccessListener {
                                var userProfile: Uri? = null
                                FirebaseStorage.getInstance().reference.child("userImages")
                                    .child("${userModel.id}/photo").downloadUrl
                                    .addOnSuccessListener {
                                        userProfile = it
                                        editedUserModel = Usermodel(
                                            userModel.id, editedName, userModel.email, editedPass, userModel.call, userProfile.toString())
                                        databaseReference.setValue(editedUserModel).addOnSuccessListener {
                                            pref.saveData("name", editedName)
                                            pref.saveData("profile", userProfile.toString())
                                            Toast.makeText(this, "프로필 수정이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                                            val intent = Intent(applicationContext, SettingsActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }.addOnFailureListener {
                                            Toast.makeText(this, "프로필 수정이 실패하였습니다.\n", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                    }
                } else {
                    Toast.makeText(this, "비밀번호를 확인하세요!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}