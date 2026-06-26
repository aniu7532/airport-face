package com.arcsoft.arcfacedemo.network

import com.arcsoft.arcfacedemo.data.http.JsonCallback
import com.arcsoft.arcfacedemo.entity.Base
import com.arcsoft.arcfacedemo.entity.CheckUnit
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * 申办单位数据仓库，负责拉取并缓存简易单位列表。
 */
object CheckUnitRepository {

    private var cached: List<CheckUnit>? = null

    /**
     * 获取申办单位简易列表，默认使用内存缓存。
     *
     * @param forceRefresh 为 true 时强制重新请求
     */
    suspend fun fetchSimpleList(forceRefresh: Boolean = false): List<CheckUnit> {
        if (!forceRefresh) {
            cached?.let { return it }
        }
        return suspendCancellableCoroutine { continuation ->
            val request = OkGo.get<Base<List<CheckUnit?>?>>(UrlConstants.checkUnitSimpleList)
                .tag(UrlConstants.checkUnitSimpleList)
            request.headers("tenant-id", "1")
            if (ApiUtils.accessToken != null) {
                request.headers("Authorization", "Bearer ${ApiUtils.accessToken}")
            }
            request.execute(object : JsonCallback<Base<List<CheckUnit?>?>?>() {
                override fun onSuccess(response: Response<Base<List<CheckUnit?>?>?>?) {
                    val list = response?.body()?.data?.filterNotNull().orEmpty()
                    cached = list
                    continuation.resume(list)
                }

                override fun onError(response: Response<Base<List<CheckUnit?>?>?>) {
                    continuation.resume(cached.orEmpty())
                }
            })
        }
    }

    /**
     * 清除内存缓存，下次请求将重新拉取。
     */
    fun invalidateCache() {
        cached = null
    }
}
