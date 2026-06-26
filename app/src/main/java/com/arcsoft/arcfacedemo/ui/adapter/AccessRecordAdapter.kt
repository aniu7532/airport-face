package com.arcsoft.arcfacedemo.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.arcsoft.arcfacedemo.databinding.AccessRecordItemBinding
import com.arcsoft.arcfacedemo.databinding.AccessRecordListHeaderBinding
import com.arcsoft.arcfacedemo.entity.CardRecords
import com.arcsoft.arcfacedemo.network.ApiUtils
import com.arcsoft.arcfacedemo.network.UrlConstants
import com.arcsoft.arcfacedemo.util.glide.EncryptedGlideFile
import com.blankj.utilcode.util.ObjectUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import java.io.File

/**
 * 通行记录分页列表适配器，展示单条通行查验信息及现场/抓拍照片。
 */
class AccessRecordAdapter : PagingDataAdapter<CardRecords.ListDTO, AccessRecordAdapter.VH>(
    DiffCallback()
) {

    /** 与列表头部适配器拼接为完整列表。 */
    fun withListHeader(header: AccessRecordHeaderAdapter): ConcatAdapter =
        ConcatAdapter(header, this)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        position: Int
    ) = VH(AccessRecordItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(
        vh: VH,
        position: Int
    ) {
        getItem(position)?.let {
            vh.bind(it)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CardRecords.ListDTO>() {

        override fun areItemsTheSame(
            p0: CardRecords.ListDTO,
            p1: CardRecords.ListDTO
        ) = p0.id === p1.id

        override fun areContentsTheSame(
            p0: CardRecords.ListDTO,
            p1: CardRecords.ListDTO
        ) = p0 == p1

    }

    class VH(val binding: AccessRecordItemBinding) : RecyclerView.ViewHolder(binding.root) {

        /** 将通行记录数据绑定到列表项视图。 */
        fun bind(value: CardRecords.ListDTO) {
            binding.apply {
                tvName.text = value.nickname
                tvCardNo.text = "证件编号：${value.idCode ?: ""}"
                tvCompany.text = "申办单位：${value.companyName ?: ""}"
                tvCheckBy.text = "查验人员：${value.checkUserName ?: ""}"
                tvArea.text = "通行道口：${value.areaName ?: ""}"
                tvTime.text = "通行时间：${value.checkTime ?: ""}"
                tvDeviceCode.text = "设备编号：${value.deviceCode ?: ""}"
                // 通行方向（1：进，-1出，2：核验)
                tvDirection.text = "通行方向：${
                    when (value.direction) {
                        1 -> "进"
                        -1 -> "出"
                        2 -> "核验"
                        else -> ""
                    }
                }"
                tvMark.text = "核实备注：${value.verifyRemark ?: ""}"

                val photo =
                    if (value.sitePhoto.isNullOrEmpty()) value.checkPhoto else value.sitePhoto
                if (ObjectUtils.isEmpty(photo)) {
                    return
                }

                if (
                    photo.startsWith("http") ||
                    photo.startsWith("encrypted/")
                ) {
                    // 拼接基础下载地址
                    val baseUrl =
                        UrlConstants.fileStreamUrl(photo)
                    // 构造带有 Authorization 头的 GlideUrl
                    val headersBuilder = LazyHeaders.Builder()
                    // 携带 accessToken（如果存在）
                    if (ApiUtils.getAccessToken() != null) {
                        headersBuilder.addHeader(
                            "Authorization",
                            "Bearer " + ApiUtils.getAccessToken()
                        )
                    }
                    val glideUrl = GlideUrl(baseUrl, headersBuilder.build())

                    Glide.with(imgAvatar.context).load(glideUrl)
                        .into(imgAvatar)
                } else {
                    val file: File = File(photo)
                    if (!file.exists()) {
                        return
                    }
                    // 加载本地加密文件
                    Glide.with(imgAvatar.context)
                        .load(EncryptedGlideFile(file))
                        .into(imgAvatar)
                }
            }
        }

    }

}

/**
 * 通行记录列表顶部标题行，固定展示「通行记录列表」。
 */
class AccessRecordHeaderAdapter : RecyclerView.Adapter<AccessRecordHeaderAdapter.HeaderVH>() {

    private var total: Int? = null

    fun setTotal(total: Int?) {
        if (this.total == total) return
        this.total = total
        notifyItemChanged(0)
    }

    override fun getItemCount(): Int = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderVH {
        val binding = AccessRecordListHeaderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return HeaderVH(binding)
    }

    override fun onBindViewHolder(holder: HeaderVH, position: Int) {
        holder.binding.tvHeaderTitle.text = "通行记录列表"
    }

    class HeaderVH(val binding: AccessRecordListHeaderBinding) :
        RecyclerView.ViewHolder(binding.root)
}