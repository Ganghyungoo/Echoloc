package com.example.echoloc.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.echoloc.GDirectionActivity
import com.example.echoloc.R
import com.example.echoloc.adapter.MemberAdapter.*
import com.example.echoloc.database.Pref
import com.example.echoloc.model.LocationModel
import com.example.echoloc.model.Usermodel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.*

class MemberAdapter (var context: Context, var group_id: String)
    : RecyclerView.Adapter<ViewHolder>(){

    var list = mutableListOf<Usermodel>()
    var pref: Pref = Pref(context)
    lateinit var eLatLng: LatLng
    var databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Echoloc").child("location").child(group_id)

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val tv_name = itemView.findViewById<TextView>(R.id.tv_memberName)
        private val iv_profile = itemView.findViewById<ImageView>(R.id.iv_memberProfile)

        fun bind(item: Usermodel) {
            tv_name.text = item.name
            Glide.with(itemView).load(Uri.parse(item.profileImageUrl)).into(iv_profile)
            iv_profile.clipToOutline = true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.rv_member_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])

        holder.itemView.setOnClickListener {
            val mDialogView = LayoutInflater.from(context).inflate(R.layout.activity_profile, null)
            val mBuilder = AlertDialog.Builder(context)
            mBuilder.setView(mDialogView)

            val mAlertDialog = mBuilder.show()

            val mUserProfileImg = mDialogView.findViewById<ImageView>(R.id.iv_profileImg)
            Glide.with(context).load(list[position].profileImageUrl).into(mUserProfileImg)

            val mUserName = mDialogView.findViewById<TextView>(R.id.tv_userName)
            mUserName.text = list[position].name.trim()

            val mUserCall = mDialogView.findViewById<TextView>(R.id.tv_userCall)
            mUserCall.text = list[position].call.trim()

            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        val locationModel = data.getValue(LocationModel::class.java)
                        if (locationModel!!.user_id == list[position].id){
                            eLatLng = LatLng(locationModel!!.latitude, locationModel.longitude)
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

                val intent = Intent(context, GDirectionActivity::class.java)
                intent.putExtra("sName", pref.getData("name"))
                intent.putExtra("eName", list[position].name)
                intent.putExtra("sLat", pref.getData("latitude").toDouble())
                intent.putExtra("sLon", pref.getData("longitude").toDouble())
                intent.putExtra("eLat", eLatLng.latitude)
                intent.putExtra("eLon", eLatLng.longitude)
                intent.putExtra("group_id", group_id)
                intent.putExtra("profile", list[position].profileImageUrl)
                context.startActivity(intent)
                mAlertDialog.dismiss()
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}