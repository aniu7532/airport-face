package com.arcsoft.arcfacedemo.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.arcsoft.arcfacedemo.data.http.JsonCallback
import com.arcsoft.arcfacedemo.entity.Base
import com.arcsoft.arcfacedemo.entity.InOutStatisticsResult
import com.arcsoft.arcfacedemo.network.ApiUtils
import com.arcsoft.arcfacedemo.network.UrlConstants
import com.arcsoft.arcfacedemo.util.dayBefore
import com.arcsoft.arcfacedemo.util.dayEnd
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class InOutStatisticsViewModel : ViewModel() {

    private val _startTime = MutableStateFlow(Calendar.getInstance().dayBefore(7))
    val startTime = _startTime.asStateFlow()

    private val _endTime = MutableStateFlow(Calendar.getInstance().dayEnd)
    val endTime = _endTime.asStateFlow()

    private val _companyName = MutableStateFlow("")
    val companyName = _companyName.asStateFlow()

    private val _list = MutableStateFlow<List<InOutStatisticsResult>>(emptyList())
    val list = _list.asStateFlow()

    fun setStartTime(startTime: Calendar) {
        _startTime.value = startTime
    }

    fun setEndTime(endTime: Calendar) {
        _endTime.value = endTime
    }

    fun request() {
        val request =
            OkGo.get<Base<List<InOutStatisticsResult?>?>?>(UrlConstants.checkRecordStatisticNeedVerify)
                .tag(UrlConstants.checkRecordStatisticNeedVerify)
        request.headers("tenant-id", "1")
        if (ApiUtils.accessToken != null) {
            request.headers("Authorization", "Bearer " + ApiUtils.accessToken)
        }
        _startTime.value?.let {
            request.params("startCheckTime", formatCheckTime(it))
        }
        _endTime.value?.let {
            request.params("endCheckTime", formatCheckTime(it))
        }
        if (_companyName.value.isNotEmpty()) {
            request.params("companyName", _companyName.value)
        }
        request.execute(object : JsonCallback<Base<List<InOutStatisticsResult?>?>?>() {
            override fun onSuccess(response: Response<Base<List<InOutStatisticsResult?>?>?>?) {
                _list.value = response?.body()?.data?.filterNotNull() ?: emptyList()
            }

            override fun onError(response: Response<Base<List<InOutStatisticsResult?>?>?>) {
            }
        })
    }

    private fun formatCheckTime(cal: Calendar): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(cal.time)

    fun reset() {
        _startTime.value = Calendar.getInstance().dayBefore(7)
        _endTime.value = Calendar.getInstance().dayEnd
        _companyName.value = ""
        request()
    }

    fun setCompanyName(companyName: String) {
        _companyName.value = companyName
    }

}