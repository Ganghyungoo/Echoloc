package com.example.echoloc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.echoloc.database.Pref
import com.example.echoloc.model.MessageModel
import com.example.echoloc.model.RoomModel
import com.example.echoloc.model.Usermodel
import com.example.echoloc.util.getDateTime
import com.example.echoloc.util.showToast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_create_room.*
import kotlinx.android.synthetic.main.activity_create_room.btn_back
import kotlinx.android.synthetic.main.activity_public_group_chatting.*

class CreateRoom : AppCompatActivity() ,View.OnClickListener{
    private lateinit var databaseReference: DatabaseReference
    private lateinit var databaseReference1: DatabaseReference
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
        databaseReference1 = database.getReference("Echoloc").child("chattings")

        usermodel=Usermodel(pref.getData("id"),pref.getData("name"),pref.getData("email"),"",pref.getData("call"));
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
            var model = MessageModel(pref.getData("name")+"님이 그룹을 생성했습니다.", getDateTime(), pref.getData("id"), pref.getData("name"), "1", "0")

            databaseReference=database.getReference("Echoloc").child("public")
            var key=databaseReference.push().key

            var key1 = databaseReference1.push().key
            databaseReference1.child(key!!).child(key1!!).setValue(model)

            var roomModel=RoomModel(key!!,roomname,0,usermodel.name,usermodel.id,usermodel.call)
            databaseReference.child(key).setValue(roomModel).addOnCompleteListener {
                showToast(applicationContext,"그룹 방 생성 완료")
                finish()

            }

        }
    }
}