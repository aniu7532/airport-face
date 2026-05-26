package com.arcsoft.arcfacedemo.network

import com.arcsoft.arcfacedemo.data.http.JsonCallback
import com.arcsoft.arcfacedemo.entity.Base
import com.arcsoft.arcfacedemo.entity.CheckUnit
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object CheckUnitRepository {

    private var cached: List<CheckUnit>? = null

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

    fun invalidateCache() {
        cached = null
    }
}
