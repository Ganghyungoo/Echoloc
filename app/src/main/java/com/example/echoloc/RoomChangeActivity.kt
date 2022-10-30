package com.example.echoloc

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.echoloc.database.Pref
import com.example.echoloc.model.RoomModel
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_room_change.*

class RoomChangeActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var pref: Pref
    lateinit var database: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    private lateinit var roommodel: RoomModel
    private lateinit var editedRoomModel: RoomModel
    private lateinit var editedName: String
    private lateinit var editedPass: String
    lateinit var group_id: String
    var flag = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_change)

        back_btn.setOnClickListener(this)
        roomchange_btn.setOnClickListener(this)
        roomdelete_btn.setOnClickListener(this)
        group_id = intent.extras!!.getString("group_id", "")
        pref = Pref(applicationContext)
        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("Echoloc").child("public").child(group_id)

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                roommodel = snapshot.getValue(RoomModel::class.java)!!
                Glide.with(this@RoomChangeActivity).load(roommodel.admin_profileImageUrl).into(iv_roomImg)
                iv_roomImg.clipToOutline = true
                change_roomname.hint = roommodel.roomname
                change_roompass.hint = roommodel.roompass
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onClick(p0: View?) {
        when(p0) {
            back_btn -> {
                onBackPressed()
            }
            roomchange_btn->{
                if (change_roomname.text.toString().isEmpty()) {
                    editedName = roommodel.roomname
                } else {
                    editedName = change_roomname.text.toString()
                }

                if (change_roompass.text.toString().isEmpty()) {
                    editedPass = roommodel.roompass
                    flag = true
                } else {
                    if (change_roompass.text.toString().equals(roompass_check.text.toString())||change_roompass.text.toString().equals(roompass_check.hint.toString())) {
                        editedPass = roompass_check.text.toString()
                        flag = true
                    } else {
                        flag = false
                    }
                }

                if(flag){
                    editedRoomModel= RoomModel(
                        roommodel.group_id,editedName,editedPass,roommodel.admin_name,roommodel.admin_id,roommodel.admin_call,roommodel.admin_profileImageUrl)
                        databaseReference.setValue(editedRoomModel).addOnSuccessListener{
                            pref.saveData("roomname",editedName)
                            pref.saveData("roompass",editedPass)
                            Toast.makeText(this, "방 수정이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                            val intent = Intent(applicationContext, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }.addOnFailureListener{
                            Toast.makeText(this, "방 수정에 실패하였습니다.\n", Toast.LENGTH_SHORT).show()
                        }
                }else {
                    Toast.makeText(this, "비밀번호를 확인하세요!", Toast.LENGTH_SHORT).show()
                }
            }
            roomdelete_btn->{
                database.getReference("Echoloc").child("public").child(group_id).removeValue()
                    Toast.makeText(this, "방이 삭제되었습니다", Toast.LENGTH_SHORT).show()
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)
                    finish()
            }


        }
    }
}