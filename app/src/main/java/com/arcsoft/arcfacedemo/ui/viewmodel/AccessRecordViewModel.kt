package com.arcsoft.arcfacedemo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.arcsoft.arcfacedemo.ui.pagingsource.AccessRecordPagingSource
import com.arcsoft.arcfacedemo.ui.pagingsource.CheckRecordQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AccessRecordViewModel : ViewModel() {

    val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    val _cardNo = MutableStateFlow("")
    val cardNo = _cardNo.asStateFlow()

    val _startTime = MutableStateFlow<Calendar?>(null)
    val startTime = _startTime.asStateFlow()

    val _endTime = MutableStateFlow<Calendar?>(null)
    val endTime = _endTime.asStateFlow()

    private val pagingConfig = PagingConfig(
        pageSize = 10,
        prefetchDistance = 1,
        enablePlaceholders = false,
    )

    /**
     * 进入页面时初始为「空条件」会拉第一页；之后仅在 [search] 时更新。
     * version 递增保证同一条件重复点击也会重新拉数。
     */
    private val searchSnapshot = MutableStateFlow(Pair(CheckRecordQuery(), 0L))

    val cardRecords = searchSnapshot.flatMapLatest { (q, _) ->
        Pager(
            config = pagingConfig,
            pagingSourceFactory = {
                AccessRecordPagingSource(q)
            },
        ).flow
    }.cachedIn(viewModelScope)

    fun reset() {
        _name.value = ""
        _cardNo.value = ""
        _startTime.value = null
        _endTime.value = null
        search()
    }

    fun search() {
        val q = CheckRecordQuery(
            nickname = _name.value,
            idCode = _cardNo.value,
            startCheckTime = formatCheckTime(_startTime.value),
            endCheckTime = formatCheckTime(_endTime.value),
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

}