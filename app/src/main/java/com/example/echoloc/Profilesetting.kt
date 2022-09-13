package com.example.echoloc

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.echoloc.model.Usermodel
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_profile_setting.*

class Profilesetting : Fragment() {
    companion object{
        var imageUri : Uri? = null
        val fireStorage = FirebaseStorage.getInstance().reference
        lateinit var database: FirebaseDatabase
        lateinit var databaseReference: DatabaseReference
        val id=databaseReference.key.toString()
        fun newInstance() : Profilesetting {
            return Profilesetting()
        }
    }
    //메모리에 올라갔을 때
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    //프레그먼트를 포함하는 액티비티에 붙혔을 때
    override fun onAttach(context: Context) {
        super.onAttach(context)
    }


    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result: ActivityResult ->
            if(result.resultCode == AppCompatActivity.RESULT_OK) {
                imageUri = result.data?.data //이미지 경로 원본
                profile_imageview.setImageURI(imageUri) //이미지 뷰를 바꿈
                //기존 사진 삭제 후 새로운 사진 등록
                fireStorage.child("userImages/$id/photo").delete().addOnSuccessListener {
                    fireStorage.child("userImages/$id/photo").putFile(imageUri!!).addOnSuccessListener {
                        fireStorage.child("userImages/$id/photo").downloadUrl.addOnSuccessListener {
                            val photoUri : Uri = it
                            println("$photoUri")
                            databaseReference.child("Users/$id/profileImageUrl").setValue(photoUri.toString())
                            Toast.makeText(requireContext(), "프로필사진이 변경되었습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                Log.d("이미지", "성공")
            }
            else{
                Log.d("이미지", "실패")
            }
        }
    //뷰가 생성되었을 때
    //프레그먼트와 레이아웃을 연결시켜주는 부분
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_setting, container, false)
        val photo = view?.findViewById<ImageView>(R.id.profile_imageview)
        val email = view?.findViewById<TextView>(R.id.profile_textview_email)
        val name = view?.findViewById<TextView>(R.id.profile_textview_name)
        val button = view?.findViewById<Button>(R.id.profile_button)
        databaseReference.child("users").child(id.toString()).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }
            override fun onDataChange(postsnapshot: DataSnapshot) {
                val userProfile=postsnapshot.getValue(Usermodel::class.java)
                println(userProfile)
                Glide.with(requireContext()).load(userProfile?.profileImageUrl)
                    .apply(RequestOptions().circleCrop())
                    .into(photo!!)
                email?.text = userProfile?.email
                name?.text = userProfile?.name
            }
        })
        //프로필사진 바꾸기
        photo?.setOnClickListener{
            val intentImage = Intent(Intent.ACTION_PICK)
            intentImage.type = MediaStore.Images.Media.CONTENT_TYPE
            getContent.launch(intentImage)
        }
        button?.setOnClickListener{
            if(name?.text!!.isNotEmpty()) {
                databaseReference.child("Users/$id/name").setValue(name.text.toString())
                name.clearFocus()
                Toast.makeText(requireContext(), "이름이 변경되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }

}