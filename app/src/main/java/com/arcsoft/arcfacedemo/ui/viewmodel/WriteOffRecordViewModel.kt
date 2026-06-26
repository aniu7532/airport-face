package com.arcsoft.arcfacedemo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.arcsoft.arcfacedemo.ui.pagingsource.CheckRecordQuery
import com.arcsoft.arcfacedemo.ui.pagingsource.WriteOffRecordPagingSource
import com.arcsoft.arcfacedemo.util.dayEnd
import com.arcsoft.arcfacedemo.util.dayStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * 核销记录（有进无出）页 ViewModel：管理筛选条件，分页加载待核实记录及总条数。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WriteOffRecordViewModel : ViewModel() {

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _cardNo = MutableStateFlow("")
    val cardNo = _cardNo.asStateFlow()

    private val _startTime = MutableStateFlow(Calendar.getInstance().dayStart)
    val startTime = _startTime.asStateFlow()

    private val _endTime = MutableStateFlow(Calendar.getInstance().dayEnd)
    val endTime = _endTime.asStateFlow()

    private val _companyName = MutableStateFlow("")
    val companyName = _companyName.asStateFlow()

    private val pagingConfig = PagingConfig(
        pageSize = 10,
        prefetchDistance = 1,
        enablePlaceholders = false,
    )

    /**
     * 进入页面时初始为「空条件」会拉第一页；之后仅在 [search] 时更新。
     * version 递增保证同一条件重复点击也会重新拉数。
     */
    private val searchSnapshot = MutableStateFlow(
        Pair(
            CheckRecordQuery(
                nickname = _name.value,
                idCode = _cardNo.value,
                startCheckTime = formatCheckTime(_startTime.value),
                endCheckTime = formatCheckTime(_endTime.value),
                companyName = _companyName.value,
            ), 0L
        )
    )

    /** 当前查询条件下接口返回的总条数（首页成功后才有值）；切换查询会先变为 null。 */
    private val _listTotal = MutableStateFlow<Int?>(null)
    val listTotal = _listTotal.asStateFlow()

    val cardRecords = searchSnapshot.flatMapLatest { (q, _) ->
        _listTotal.value = null
        Pager(
            config = pagingConfig,
            pagingSourceFactory = {
                WriteOffRecordPagingSource(q) { total ->
                    _listTotal.value = total
                }
            },
        ).flow
    }.cachedIn(viewModelScope)

    /** 重置筛选条件为默认值并触发查询。 */
    fun reset() {
        _name.value = ""
        _cardNo.value = ""
        _startTime.value = Calendar.getInstance().dayStart
        _endTime.value = Calendar.getInstance().dayEnd
        _companyName.value = ""
        search()
    }

    /** 按当前筛选条件发起分页查询。 */
    fun search() {
        val q = CheckRecordQuery(
            nickname = _name.value,
            idCode = _cardNo.value,
            startCheckTime = formatCheckTime(_startTime.value),
            endCheckTime = formatCheckTime(_endTime.value),
            companyName = _companyName.value,
        )
        val nextV = searchSnapshot.value.second + 1
        searchSnapshot.value = Pair(q, nextV)
    }

    private fun formatCheckTime(cal: Calendar?): String {
        if (cal == null) return ""
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(cal.time)
    }

    fun setName(name: String) {
        _name.value = name
    }

    fun setCardNo(cardNo: String) {
        _cardNo.value = cardNo
    }

    fun setStartTime(startTime: Calendar) {
        _startTime.value = startTime
    }

    fun setEndTime(endTime: Calendar) {
        _endTime.value = endTime
    }

    fun setCompanyName(companyName: String) {
        _companyName.value = companyName
    }

}