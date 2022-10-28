package com.example.echoloc

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.echoloc.adapter.MemberAdapter
import com.example.echoloc.adapter.PublicChattingAdapter
import com.example.echoloc.database.Pref
import com.example.echoloc.model.MessageModel
import com.example.echoloc.model.Usermodel
import com.example.echoloc.util.getDateTime
import com.example.echoloc.util.showToast
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import kotlinx.android.synthetic.main.activity_public_group_chatting.*
import kotlinx.android.synthetic.main.drawerview_member_list.*

class PublicGroupChatting : AppCompatActivity(), View.OnClickListener {

    lateinit var database: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference

    var group_id = ""
    var admin_id : String? = null
    lateinit var pref: Pref
    lateinit var adapter: PublicChattingAdapter
    lateinit var list: ArrayList<MessageModel>

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var drawerView: View

    lateinit var memberAdapter: MemberAdapter
    var memberList = mutableListOf<Usermodel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_public_group_chatting)
        btn_back.setOnClickListener(this)
        btn_messagesend.setOnClickListener(this)
        btn_member.setOnClickListener(this)

        database = FirebaseDatabase.getInstance()
        pref = Pref(applicationContext)
        databaseReference = database.getReference("Echoloc").child("chattings")
        group_id = intent.extras!!.getString("group_id", "")
        list = ArrayList()
        var manager = LinearLayoutManager(applicationContext)
        manager.stackFromEnd = true
        recyclerview.layoutManager=manager
        adapter = PublicChattingAdapter(list, pref.getData("id"))
        recyclerview.adapter = adapter

        // 네비게이션 바
        drawerLayout = findViewById(R.id.drawer_memberList)
        drawerView = findViewById(R.id.drawerView_member)

        drawerLayout.setDrawerListener(drawerListener)
        drawerView.setOnTouchListener { v, event -> true }

        database.getReference("Echoloc").child("public").child(group_id).child("roomname")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    Room_name.text = snapshot.value.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        database.getReference("Echoloc").child("public").child(group_id).child("admin_id")
            .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                admin_id = snapshot.value.toString()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        getChatMessage()
    }

    var drawerListener: DrawerListener = object : DrawerListener {
        override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
        }

        override fun onDrawerOpened(drawerView: View) {
        }

        override fun onDrawerClosed(drawerView: View) {
        }

        override fun onDrawerStateChanged(newState: Int) {
        }

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

    // 그룹원 리스트 표시
    private fun getMemberList() {
        memberAdapter = MemberAdapter(this, group_id)
        member_adapter.adapter = memberAdapter

        database.getReference("Echoloc").child("Users")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        val userModel = data.getValue(Usermodel::class.java)

                        if (admin_id == userModel!!.id) {
                            tv_adminName.text = userModel!!.name
                            Glide.with(this@PublicGroupChatting).load(Uri.parse(userModel.profileImageUrl)).into(iv_adminProfile)
                            iv_adminProfile.clipToOutline = true
                            break
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

        database.getReference("Echoloc").child("public").child(group_id).child("members")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    memberList.clear()

                    for (data in snapshot.children) {
                        val userModel = data.getValue(Usermodel::class.java)

                        if (pref.getData("id") != userModel!!.id)
                            memberList.add(userModel)
                    }

                    memberAdapter.list = memberList
                    memberAdapter.notifyDataSetChanged()

                }

                override fun onCancelled(error: DatabaseError) {
                    showToast(this@PublicGroupChatting, error.message)
                }

            })
    }

    override fun onClick(p0: View?) {
        when (p0) {
            btn_back -> {
                onBackPressed()
            }
            btn_member -> {
//                var intent=Intent(applicationContext, MemberListActivity::class.java)
//                intent.putExtra("group_id", group_id)
//                intent.putExtra("isfrom_public","1")
//                startActivity(intent)

                getMemberList()
                drawerLayout.openDrawer(drawerView)
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
                    showToast(applicationContext, "메세지를 입력해주세요!")
                }

            }
        }
    }
}