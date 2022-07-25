package com.example.echoloc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.echoloc.adapter.PrivateAdapter
import com.example.echoloc.adapter.PrivateChattingAdapter
import com.example.echoloc.adapter.PublicChattingAdapter
import com.example.echoloc.database.Pref
import com.example.echoloc.model.MessageModel
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_private_group_chatting.*
import kotlinx.android.synthetic.main.activity_private_group_chatting.btn_back
import kotlinx.android.synthetic.main.activity_private_group_chatting.recyclerview
import kotlinx.android.synthetic.main.activity_public_group_chatting.*

class PrivateGroupChatting : AppCompatActivity(), View.OnClickListener {
    lateinit var adapter: PrivateChattingAdapter
    lateinit var list: ArrayList<MessageModel>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_private_group_chatting)
        btn_back.setOnClickListener(this)
//        database = FirebaseDatabase.getInstance()
//        pref = Pref(applicationContext)
//        databaseReference = database.getReference("Echoloc").child("chattings")
//        group_id = intent.extras!!.getString("group_id", "")
//
        list = ArrayList()
        var model1=MessageModel("test","dfdf","dfdfdfsd","sdfdfdfgffdf","1","d")
        var model2=MessageModel("test","dfdf","dfdfdfsd","sdfdfdfgffdf","2","d")
        var model3=MessageModel("test","dfdf","dfdfdfsd","sdfdfdfgffdf","1","d")
        var model4=MessageModel("test","dfdf","dfdfdfsd","sdfdfdfgffdf","0","d")
        list.add(model1)
        list.add(model2)
        list.add(model3)
        list.add(model4)


        var manager = LinearLayoutManager(applicationContext)
        manager.stackFromEnd = true
        recyclerview.layoutManager=manager
        adapter = PrivateChattingAdapter(list, "1")
        recyclerview.adapter = adapter
    }

    override fun onClick(p0: View?) {
        when (p0) {
            btn_back -> {
                onBackPressed()
            }
        }
    }
}