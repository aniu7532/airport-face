package com.arcsoft.arcfacedemo.ui.pagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.arcsoft.arcfacedemo.data.http.JsonCallback
import com.arcsoft.arcfacedemo.entity.Base
import com.arcsoft.arcfacedemo.entity.CardRecords
import com.arcsoft.arcfacedemo.network.ApiUtils
import com.arcsoft.arcfacedemo.network.UrlConstants
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * 通行记录分页数据源，按页请求需核实的查验通行记录接口。
 */
class AccessRecordPagingSource(
    private val query: CheckRecordQuery = CheckRecordQuery()
) : PagingSource<Int, CardRecords.ListDTO>() {

    /** 加载指定页码的通行记录列表。 */
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CardRecords.ListDTO> {
        return try {
            val pageIndex = params.key ?: 1
            val list = loadData(pageIndex, query)
            LoadResult.Page(
                data = list,
                prevKey = if (pageIndex == 1) null else pageIndex - 1,
                nextKey = if (list.isEmpty()) null else pageIndex + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    /** 发起网络请求获取单页通行记录数据。 */
    suspend fun loadData(pageNo: Int, query: CheckRecordQuery): List<CardRecords.ListDTO> {
        return suspendCancellableCoroutine { continuation ->
            val request =
                OkGo.get<Base<CardRecords?>?>(UrlConstants.checkRecordPageNeedVerify)
                    .tag(UrlConstants.checkRecordPageNeedVerify)
            request.headers("tenant-id", UrlConstants.TENANT_ID)
            if (ApiUtils.accessToken != null) {
                request.headers("Authorization", "Bearer " + ApiUtils.accessToken)
            }
            request.params("pageNo", pageNo)
                .params("pageSize", 10)
                .params("nickname", query.nickname)
                .params("idCode", query.idCode)
                .params("startCheckTime", query.startCheckTime)
                .params("endCheckTime", query.endCheckTime)
            if (query.companyName.isNotEmpty()) {
                request.params("companyName", query.companyName)
            }
            request.execute(object : JsonCallback<Base<CardRecords?>?>() {
                    override fun onSuccess(response: Response<Base<CardRecords?>?>?) {
                        val data = response?.body()?.data
                        val list = data?.list ?: emptyList()
                        continuation.resume(list)
                    }

                    override fun onError(response: Response<Base<CardRecords?>?>) {
                        continuation.resume(emptyList())
                    }
                })
        }
    }

    override fun getRefreshKey(state: PagingState<Int, CardRecords.ListDTO>) = 1
}