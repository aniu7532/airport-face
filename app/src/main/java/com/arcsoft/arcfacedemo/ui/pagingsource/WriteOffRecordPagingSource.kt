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
 * 核销记录列表查询条件（与接口 query 参数一致，按需扩展字段）。
 */
data class CheckRecordQuery(
    val nickname: String = "",
    val idCode: String = "",
    val startCheckTime: String = "",
    val endCheckTime: String = "",
    val companyName: String = "",
)

/**
 * 核销记录（有进无出）分页数据源，按页请求待核实记录并在首页回传总条数。
 */
class WriteOffRecordPagingSource(
    private val query: CheckRecordQuery = CheckRecordQuery(),
    /** 仅第一页会带上接口里的总条数，用于 UI（如列表 header）。 */
    private val onQueryTotal: ((Int) -> Unit)? = null,
) : PagingSource<Int, CardRecords.ListDTO>() {

    /** 加载指定页码的核销记录列表。 */
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

    /** 发起网络请求获取单页核销记录数据，首页成功时回调总条数。 */
    suspend fun loadData(pageNo: Int, query: CheckRecordQuery): List<CardRecords.ListDTO> {
        return suspendCancellableCoroutine { continuation ->
            val request =
                OkGo.get<Base<CardRecords?>?>(UrlConstants.checkRecordPageNeedVerifyNoOut)
                    .tag(UrlConstants.checkRecordPageNeedVerifyNoOut)
            request.headers("tenant-id", "1")
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
                        if (pageNo == 1) {
                            data?.let { onQueryTotal?.invoke(it.total) }
                        }
                        continuation.resume(list)
                    }

                    override fun onError(response: Response<Base<CardRecords?>?>) {
                        continuation.resume(emptyList())
                    }
                })
        }
    }

    override fun getRefreshKey(state: PagingState<Int, CardRecords.ListDTO>): Int? {
        return 1
    }
}