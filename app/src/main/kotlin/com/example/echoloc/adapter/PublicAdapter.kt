package com.example.echoloc.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.echoloc.GMapsActivity
import com.example.echoloc.R
import com.example.echoloc.RoomChangeActivity
import com.example.echoloc.model.RoomModel
import com.example.echoloc.util.showToast
import kotlinx.android.synthetic.main.rv_layout.view.*

class PublicAdapter(
    var context: Context,
    var list: ArrayList<RoomModel>,
    var user_id: String,
    var ongroupJoin: PrivateAdapter.onClick
) :RecyclerView.Adapter<PublicAdapter.ViewHolder>(){

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var joinroom = itemView.joinroom
        var txt_roomname = itemView.txt_roomname
        var roomset=itemView.roomset
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txt_roomname.text = list[position].roomname

        if (list[position].isgroupjoined || list[position].admin_id == user_id) {
            holder.joinroom.visibility = View.GONE
        } else {
            holder.joinroom.visibility = View.VISIBLE
        }
        if(list[position].admin_id==user_id){
            holder.roomset.visibility=View.VISIBLE
        }else{
            holder.roomset.visibility=View.GONE
        }
        fun showSettingPopup(){
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.pass_popup, null)
            val text: EditText = view.findViewById(R.id.room_et_pass)
            val alertDialog= AlertDialog.Builder(context)
                .setTitle("방 비밀번호").setPositiveButton("입장하기") { dialog, which->
                    var pass = text.text.toString()
                    if(list[position].roompass.toString()!=pass.toString()){
                        showToast(context, msg = "비밀번호가 틀립니다")
                    }
                    else{
                        ongroupJoin.onGroupJoined(list[position])
                    }
                }
                .setNegativeButton("취소",null)
                .create()
            alertDialog.setView(view)
            alertDialog.show()
        }

        holder.joinroom.setOnClickListener {
            showSettingPopup()
        }

        holder.itemView.setOnClickListener {
            if (holder.joinroom.visibility == View.GONE) {
                var intent = Intent(context,GMapsActivity::class.java)
                intent.putExtra("group_id", list[position].group_id)
                context.startActivity(intent)
            }
        }
        holder.roomset.setOnClickListener {
                var intent = Intent(context, RoomChangeActivity::class.java)
                intent.putExtra("group_id", list[position].group_id)
                context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface onClick {
        fun onGroupJoined(roomModel: RoomModel)
    }
}