package com.example.echoloc

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.echoloc.adapter.MemberAdapter
import com.example.echoloc.adapter.PublicChattingAdapter
import com.example.echoloc.database.Pref
import com.example.echoloc.model.LocationModel
import com.example.echoloc.model.MessageModel
import com.example.echoloc.model.Usermodel
import com.example.echoloc.util.getDateTime
import com.example.echoloc.util.showToast
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import kotlinx.android.synthetic.main.activity_public_group_chatting.*
import kotlinx.android.synthetic.main.drawerview_member_list.*

class PublicGroupChatting : AppCompatActivity(), View.OnClickListener {

    lateinit var database: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference

    var group_id = ""
    var admin_id : String? = null
    var admin: Usermodel? = null
    lateinit var pref: Pref
    lateinit var adapter: PublicChattingAdapter
    lateinit var list: ArrayList<MessageModel>

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var drawerView: View
    lateinit var eLatLng: LatLng
    var myKey: String? = null

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

        rl_admin.setOnClickListener {
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.activity_profile, null)
            val mBuilder = AlertDialog.Builder(this)
            mBuilder.setView(mDialogView)

            val mAlertDialog = mBuilder.show()

            val mUserProfileImg = mDialogView.findViewById<ImageView>(R.id.iv_profileImg)
            Glide.with(this).load(admin?.profileImageUrl).into(mUserProfileImg)

            val mUserName = mDialogView.findViewById<TextView>(R.id.tv_userName)
            mUserName.text = admin?.name?.trim()

            val mUserCall = mDialogView.findViewById<TextView>(R.id.tv_userCall)
            mUserCall.text = admin?.call?.trim()

            database.getReference("Echoloc").child("location").child(group_id)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (data in snapshot.children) {
                            val locationModel = data.getValue(LocationModel::class.java)
                            if (admin_id == locationModel!!.user_id) {
                                eLatLng = LatLng(locationModel.latitude, locationModel.longitude)
                                break
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
            val mNavigation = mDialogView.findViewById<Button>(R.id.btn_navigation)
            mNavigation.setOnClickListener {

                val intent = Intent(this, GDirectionActivity::class.java)
                intent.putExtra("sName", pref.getData("name"))
                intent.putExtra("eName", admin?.name)
                intent.putExtra("sLat", pref.getData("latitude").toDouble())
                intent.putExtra("sLon", pref.getData("longitude").toDouble())
                intent.putExtra("eLat", eLatLng.latitude)
                intent.putExtra("eLon", eLatLng.longitude)
                intent.putExtra("group_id", group_id)
                intent.putExtra("profile", admin?.profileImageUrl)
                startActivity(intent)
                mAlertDialog.dismiss()
            }
        }

        val mDatabaseReference = database.getReference("Echoloc").child("public").child(group_id).child("members")
        mDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    val userModel = data.getValue(Usermodel::class.java)

                    if (pref.getData("id") == userModel!!.id) {
                        myKey = data.key.toString()
                        break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        btn_exit.setOnClickListener {
            mDatabaseReference.child(myKey!!).removeValue()
            Toast.makeText(this, "그룹을 탈퇴했습니다.", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

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
                            admin = data.getValue(Usermodel::class.java)!!
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
                        "0",
                        pref.getData("profile")
                    )
                    Log.d("avd", pref.getData("profile"))
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