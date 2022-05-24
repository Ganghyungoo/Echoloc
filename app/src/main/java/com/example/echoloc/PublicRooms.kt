package com.example.echoloc

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.echoloc.adapter.PublicAdapter
import com.example.echoloc.model.RoomModel
import kotlinx.android.synthetic.main.fragment_public_rooms.*
import kotlinx.android.synthetic.main.fragment_public_rooms.view.*


class PublicRooms : Fragment() {

    lateinit var adapter: PublicAdapter
    lateinit var list:ArrayList<RoomModel>

    lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view =  inflater.inflate(R.layout.fragment_public_rooms, container, false)

        recyclerView = view.recyclerview
        getAdapter()
        return view
    }

    private fun getAdapter() {
        list = ArrayList()
        for (i in 0..5) {
            list.add(i, RoomModel("", "", 0, "", "", ""))
        }
        adapter = PublicAdapter(requireContext(), list)

        recyclerView.adapter = adapter
    }
}