package com.amigo.calllog

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    private val callLogs = CallLogHelper.getCallLogs(activity)

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MissedCallsFragment.newInstance(
                callLogs.first.flatMap { it.calls }
            )
            1 -> ReceivedCallsFragment.newInstance(
                callLogs.second.flatMap { it.calls }
            )
            2 -> DialedCallsFragment.newInstance(
                callLogs.third.flatMap { it.calls }
            )
            3 -> MessagesFragment.newInstance()
            else -> throw IllegalArgumentException()
        }
    }
}