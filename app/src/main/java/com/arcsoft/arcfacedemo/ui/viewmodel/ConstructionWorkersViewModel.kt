package com.arcsoft.arcfacedemo.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ConstructionWorkersTab(val label: String) {
    WriteOffRecord("核销记录"),
    AccessRecord("通行记录"),
    InOutStatistics("进出统计")
}

class ConstructionWorkersViewModel: ViewModel() {

    val _currentTab = MutableStateFlow(ConstructionWorkersTab.WriteOffRecord)
    val currentTab = _currentTab.asStateFlow()

    fun changeTab(tab: Int) {
        _currentTab.value = ConstructionWorkersTab.entries[tab]
    }

}