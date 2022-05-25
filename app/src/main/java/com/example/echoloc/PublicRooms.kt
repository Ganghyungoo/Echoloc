package com.example.echoloc

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.echoloc.adapter.PublicAdapter
import com.example.echoloc.database.Pref
import com.example.echoloc.model.MessageModel
import com.example.echoloc.model.RoomModel
import com.example.echoloc.model.Usermodel
import com.example.echoloc.util.getDateTime
import com.example.echoloc.util.showToast
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_public_rooms.*
import kotlinx.android.synthetic.main.fragment_public_rooms.view.*


class PublicRooms : Fragment(), PublicAdapter.onClick {

    lateinit var adapter: PublicAdapter
    lateinit var list:ArrayList<RoomModel>

    lateinit var recyclerView: RecyclerView

    lateinit var database: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var databaseReference1: DatabaseReference
    lateinit var pref: Pref

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view =  inflater.inflate(R.layout.fragment_public_rooms, container, false)
        recyclerView = view.recyclerview
        pref = Pref(requireContext())
        database=FirebaseDatabase.getInstance()
        databaseReference=database.getReference("Echoloc").child("public")
        databaseReference1 = database.getReference("Echoloc").child("chattings")

        list = ArrayList()
        adapter = PublicAdapter(requireContext(), list, pref.getData("id"), this)

        recyclerView.adapter = adapter

        getPublicRooms()
        return view
    }

    private fun getPublicRooms() {
        databaseReference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()

                for (postsnapshot in snapshot.children) {
                    var value = postsnapshot.getValue(RoomModel::class.java)
                    var isgroupjoined = false


                    for (snapshot in postsnapshot.child("members").children) {
                        var member = snapshot.getValue(Usermodel::class.java)
                        if (pref.getData("id") == member!!.id) {
                            isgroupjoined = true
                        }
                    }

                    if (isgroupjoined) {
                        value!!.isgroupjoined = true
                    }

                    list.add(value!!)
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                 showToast(context!!, error.message)
            }

        })
    }

    override fun onGroupJoined(roomModel: RoomModel) {
        var usermodel = Usermodel(
            pref.getData("id"), pref.getData("name"), pref.getData("email"), "", pref.getData("call"))

        var model = MessageModel(
            pref.getData("name")+"joined the group",
            getDateTime(), pref.getData("id"),
            pref.getData("name"),
            "1",
            "1")

        var key = databaseReference.push().key
        databaseReference.child(roomModel.group_id).child("members").child(key!!)
            .setValue(usermodel).addOnCompleteListener {
            var key1 = databaseReference1.push().key
            databaseReference1.child(key!!).child(key1!!).setValue(model)
            showToast(requireContext(), "Group joined Successfully")
        }

    }
}