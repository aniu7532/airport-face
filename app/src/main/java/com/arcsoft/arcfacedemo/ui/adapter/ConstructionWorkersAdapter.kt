package com.arcsoft.arcfacedemo.ui.adapter

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.arcsoft.arcfacedemo.ui.fragment.AccessRecordFragment
import com.arcsoft.arcfacedemo.ui.fragment.InOutStatisticsFragment
import com.arcsoft.arcfacedemo.ui.fragment.WriteOffRecordFragment
import com.arcsoft.arcfacedemo.ui.viewmodel.ConstructionWorkersTab

/**
 * 施工人员管理页 ViewPager2 适配器，按 Tab 顺序加载三个业务 Fragment。
 */
class ConstructionWorkersAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    /** 根据 Tab 位置创建对应的业务 Fragment。 */
    override fun createFragment(position: Int) = when (position) {
        ConstructionWorkersTab.WriteOffRecord.ordinal -> WriteOffRecordFragment.newInstance()
        ConstructionWorkersTab.AccessRecord.ordinal -> AccessRecordFragment.newInstance()
        ConstructionWorkersTab.InOutStatistics.ordinal -> InOutStatisticsFragment.newInstance()
        else -> throw Exception("")
    }

    override fun getItemCount() = ConstructionWorkersTab.entries.size
}