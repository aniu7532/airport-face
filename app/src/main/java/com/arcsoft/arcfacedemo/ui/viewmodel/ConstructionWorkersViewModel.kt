package com.arcsoft.arcfacedemo.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** 施工人员管理页 Tab 枚举。 */
enum class ConstructionWorkersTab(val label: String) {
    WriteOffRecord("核销记录"),
    AccessRecord("通行记录"),
    InOutStatistics("进出统计")
}

/**
 * 施工人员管理页 ViewModel，维护当前选中的 Tab 状态。
 */
class ConstructionWorkersViewModel: ViewModel() {

    val _currentTab = MutableStateFlow(ConstructionWorkersTab.WriteOffRecord)
    val currentTab = _currentTab.asStateFlow()

    /** 切换到指定 Tab（按索引）。 */
    fun changeTab(tab: Int) {
        _currentTab.value = ConstructionWorkersTab.entries[tab]
    }

}