package com.example.echoloc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.echoloc.database.Pref
import com.example.echoloc.model.RoomModel
import com.example.echoloc.model.Usermodel
import com.example.echoloc.util.showToast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_create_room.*

class CreateRoom : AppCompatActivity() ,View.OnClickListener{
    private lateinit var databaseReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var pref: Pref
    private lateinit var usermodel: Usermodel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_room)
        btn_createroom.setOnClickListener(this)
        btn_back.setOnClickListener(this)
        pref=Pref(applicationContext)
        database=FirebaseDatabase.getInstance()

        usermodel=Usermodel(pref.getData("id"),pref.getData("name"),pref.getData("email"),pref.getData("pass"),pref.getData("call"));
    }

    override fun onClick(p0: View?) {
        when(p0)
        {
            btn_createroom ->
            {
                var roomname=et_roomname.text.toString().trim()
                var isprivate=checkbox.isChecked

                if(roomname.isEmpty())
                {
                    showToast(applicationContext, msg = "방 이름을 입력하세요!")
                    return
                }
                createRoom(roomname,isprivate)

            }
            btn_back -> {
                onBackPressed()
            }
        }
    }

    private  fun createRoom(roomname:String, isprivate:Boolean)
    {
        if (isprivate)
        {
            databaseReference=database.getReference("Echoloc").child("private")
            var key=databaseReference.push().key

            var roomModel=RoomModel(key!!,roomname,1,usermodel.name,usermodel.id,usermodel.call)
            databaseReference.child(key).setValue(roomModel).addOnCompleteListener {
                showToast(applicationContext,"그룹 방 생성 완료")
                finish()
            }

        }else{
            databaseReference=database.getReference("Echoloc").child("public")
            var key=databaseReference.push().key

            var roomModel=RoomModel(key!!,roomname,0,usermodel.name,usermodel.id,usermodel.call)
            databaseReference.child(key).setValue(roomModel).addOnCompleteListener {
                showToast(applicationContext,"그룹 방 생성 완료")
                finish()

            }

        }
    }
}