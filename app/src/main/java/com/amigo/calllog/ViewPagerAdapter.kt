package com.amigo.calllog

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    private val callLogs = CallLogHelper.getCallLogs(activity)

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MissedCallsFragment.newInstance(callLogs.first)
            1 -> ReceivedCallsFragment.newInstance(callLogs.second)
            2 -> DialedCallsFragment.newInstance(callLogs.third)
            else -> throw IllegalArgumentException()
        }
    }
}