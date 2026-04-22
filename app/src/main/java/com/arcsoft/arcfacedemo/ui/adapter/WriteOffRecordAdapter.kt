package com.arcsoft.arcfacedemo.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.arcsoft.arcfacedemo.databinding.WriteOffRecordItemBinding
import com.arcsoft.arcfacedemo.databinding.WriteOffRecordListHeaderBinding
import com.arcsoft.arcfacedemo.entity.CardRecords
import com.arcsoft.arcfacedemo.network.ApiUtils
import com.arcsoft.arcfacedemo.network.UrlConstants
import com.arcsoft.arcfacedemo.widget.dialog.AppKeyPopDialog
import com.arcsoft.arcfacedemo.widget.dialog.VerifyAndConfirmDialog
import com.blankj.utilcode.util.ObjectUtils
import com.blankj.utilcode.util.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.lxj.xpopup.XPopup
import java.io.File

/**
 * 分页列表适配器。列表顶部 header 请用 [withListHeader] 与 [WriteOffRecordHeaderAdapter] 通过 [ConcatAdapter] 拼接，
 * 不要在本 Adapter 里伪造 position 偏移（Paging 的 position 与数据一一对应）。
 */
class WriteOffRecordAdapter : PagingDataAdapter<CardRecords.ListDTO, WriteOffRecordAdapter.VH>(
    DiffCallback()
) {

    fun withListHeader(header: WriteOffRecordHeaderAdapter): ConcatAdapter =
        ConcatAdapter(header, this)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        position: Int
    ) = VH(WriteOffRecordItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

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

    class VH(val binding: WriteOffRecordItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(value: CardRecords.ListDTO) {
            binding.apply {
                tvName.text = value.nickname
                tvCardNo.text = "证件编号：${value.idCode}"
                tvArea.text = "通行道口：${value.areaName}"
                tvTime.text = "通行时间：${value.checkTime}"
                tvCheckBy.text = "查验人员：${value.checkUserName}"
                if (ObjectUtils.isEmpty(value.verifyRemark)) {
                    tvMark.visibility = View.GONE
                } else {
                    tvMark.visibility = View.VISIBLE
                }
                tvMark.text = "核实备注：${value.verifyRemark}"

                btnVerify.setOnClickListener {
                    XPopup.Builder(btnVerify.context).isDestroyOnDismiss(true) // 对于只使用一次的弹窗，推荐设置这个
                        .asCustom(VerifyAndConfirmDialog(btnVerify.context, value, {
                            (bindingAdapter as WriteOffRecordAdapter).refresh()
                        })).show()
                }

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
                        UrlConstants.URL + "/app-api/infra/file/stream?path=" + photo
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
                    Glide.with(Utils.getApp())
                        .load(file)
                        .into(imgAvatar)
                }
            }
        }

    }

}

/**
 * 核销记录列表顶部说明行，固定 1 条，放在 [WriteOffRecordAdapter] 之上。
 * 总条数通过 [setTotal] 更新（来自接口 [com.arcsoft.arcfacedemo.entity.CardRecords.getTotal]）。
 */
class WriteOffRecordHeaderAdapter : RecyclerView.Adapter<WriteOffRecordHeaderAdapter.HeaderVH>() {

    private var total: Int? = null

    fun setTotal(total: Int?) {
        if (this.total == total) return
        this.total = total
        notifyItemChanged(0)
    }

    override fun getItemCount(): Int = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderVH {
        val binding = WriteOffRecordListHeaderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return HeaderVH(binding)
    }

    override fun onBindViewHolder(holder: HeaderVH, position: Int) {
        holder.binding.tvHeaderTitle.text = when (val t = total) {
            null -> "有进无出列表"
            else -> "有进无出列表（总数：$t）"
        }
    }

    class HeaderVH(val binding: WriteOffRecordListHeaderBinding) :
        RecyclerView.ViewHolder(binding.root)
}
