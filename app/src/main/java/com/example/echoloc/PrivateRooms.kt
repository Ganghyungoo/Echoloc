package com.example.echoloc

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.echoloc.adapter.PrivateAdapter
import com.example.echoloc.adapter.PublicAdapter
import com.example.echoloc.database.Pref
import com.example.echoloc.model.RoomModel
import com.example.echoloc.util.showToast
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_private_rooms.view.*
import kotlinx.android.synthetic.main.fragment_public_rooms.*
import kotlinx.android.synthetic.main.fragment_public_rooms.view.*
import kotlinx.android.synthetic.main.fragment_public_rooms.view.recyclerview

class PrivateRooms : Fragment() {

    lateinit var adapter: PrivateAdapter
    lateinit var list:ArrayList<RoomModel>

    lateinit var recyclerView: RecyclerView

    lateinit var database: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var pref: Pref

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_private_rooms, container, false)

        recyclerView = view.recyclerview_private
        pref = Pref(requireContext())
        database=FirebaseDatabase.getInstance()
        databaseReference=database.getReference("Echoloc").child("private")

        list = ArrayList()
        adapter = PrivateAdapter(requireContext(), list)

        recyclerView.adapter = adapter

        getPrivateRooms()
        return view
    }

    private fun getPrivateRooms() {
        databaseReference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()

                for (postsnapshot in snapshot.children) {
                    var value = postsnapshot.getValue(RoomModel::class.java)
                    var isAdmin = false
                    if (pref.getData("id") == value!!.admin_id) {
                        value.isAdmin(true)
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
}