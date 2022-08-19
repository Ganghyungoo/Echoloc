package com.example.echoloc

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.echoloc.adapter.PrivateChattingAdapter
import com.example.echoloc.model.MessageModel
import kotlinx.android.synthetic.main.activity_private_group_chatting.*

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

        list = ArrayList()

        var model1=MessageModel("Test","dfdf","dsfdsfsd","sdfsdfdsfsd","0","2")
        list.add(model1)


        var manager = LinearLayoutManager(applicationContext)
        manager.stackFromEnd = true
        recyclerview.layoutManager=manager
        adapter = PrivateChattingAdapter(list, "1")
        recyclerview.adapter = adapter
        print(adapter)

    }

    override fun onClick(p0: View?) {
        when (p0) {
            btn_back -> {
                onBackPressed()
            }
        }
    }
}