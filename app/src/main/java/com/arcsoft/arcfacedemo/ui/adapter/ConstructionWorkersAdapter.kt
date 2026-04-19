package com.arcsoft.arcfacedemo.ui.adapter

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.arcsoft.arcfacedemo.ui.fragment.AccessRecordFragment
import com.arcsoft.arcfacedemo.ui.fragment.InOutStatisticsFragment
import com.arcsoft.arcfacedemo.ui.fragment.WriteOffRecordFragment
import com.arcsoft.arcfacedemo.ui.viewmodel.ConstructionWorkersTab

class ConstructionWorkersAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int) = when (position) {
        ConstructionWorkersTab.WriteOffRecord.ordinal -> WriteOffRecordFragment.newInstance()
        ConstructionWorkersTab.AccessRecord.ordinal -> AccessRecordFragment.newInstance()
        ConstructionWorkersTab.InOutStatistics.ordinal -> InOutStatisticsFragment.newInstance()
        else -> throw Exception("")
    }

    override fun getItemCount() = ConstructionWorkersTab.entries.size
}