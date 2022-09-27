package com.example.echoloc.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.echoloc.PublicRooms

class ViewPagerAdapter(fa:FragmentActivity, private val fragments: ArrayList<PublicRooms>) :
    FragmentStateAdapter(fa){
    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}