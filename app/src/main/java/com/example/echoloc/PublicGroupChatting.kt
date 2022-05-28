package com.example.echoloc

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.echoloc.adapter.PublicChattingAdapter
import com.example.echoloc.database.Pref
import com.example.echoloc.model.MessageModel
import com.example.echoloc.util.getDateTime
import com.example.echoloc.util.showToast
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_public_group_chatting.*

class PublicGroupChatting : AppCompatActivity(), View.OnClickListener {
// wws
    lateinit var database: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    var group_id = ""
    lateinit var pref: Pref
    lateinit var adapter: PublicChattingAdapter
    lateinit var list: ArrayList<MessageModel>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_public_group_chatting)
        btn_back.setOnClickListener(this)
        btn_messagesend.setOnClickListener(this)
        database = FirebaseDatabase.getInstance()
        pref = Pref(applicationContext)
        databaseReference = database.getReference("Echoloc").child("chattings")
        group_id = intent.extras!!.getString("group_id", "")

        list = ArrayList()
        adapter = PublicChattingAdapter(list, pref.getData("id"))



        var manager = LinearLayoutManager(applicationContext)
        manager.stackFromEnd = true
        recyclerview.adapter = adapter
        print(adapter)

        getChatMessage()
    }

    private fun getChatMessage() {
        var reference = database.getReference("Echoloc").child("chattings").child(group_id)
        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                for (postsnapshot in snapshot.children) {
                    var message = postsnapshot.getValue(MessageModel::class.java)
                    list.add(message!!)
                }

                if (list.size > 0) {
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    override fun onClick(p0: View?) {
        when (p0) {
            btn_back -> {
                onBackPressed()
            }
            btn_messagesend -> {
                var message = et_message.text.toString().trim()

                if (message.isNotEmpty()) {
                    btn_messagesend.visibility = View.GONE
                    progressBar.visibility = View.VISIBLE

                    var model = MessageModel(
                        message,
                        getDateTime(),
                        pref.getData("id"),
                        pref.getData("name"),
                        "0",
                        "0"
                    )
                    var key = databaseReference.push().key
                    databaseReference.child(group_id).child(key!!).setValue(model)
                        .addOnCompleteListener {
                        et_message.setText("")
                        btn_messagesend.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                    }
                } else {
                    showToast(applicationContext, "Please enter message")
                }
            }
        }
    }
}