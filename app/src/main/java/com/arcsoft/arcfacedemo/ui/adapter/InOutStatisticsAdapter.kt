package com.arcsoft.arcfacedemo.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.arcsoft.arcfacedemo.R
import com.arcsoft.arcfacedemo.databinding.AccessRecordListHeaderBinding
import com.arcsoft.arcfacedemo.databinding.InOutStatisticsItemBinding
import com.arcsoft.arcfacedemo.entity.InOutStatisticsResult


class InOutStatisticsAdapter : RecyclerView.Adapter<InOutStatisticsAdapter.VH>() {

    fun withListHeader(): ConcatAdapter =
        ConcatAdapter(InOutStatisticsHeader(), this, InOutStatisticsTitle())

    val list = mutableListOf<InOutStatisticsResult>()

    fun setList(list: List<InOutStatisticsResult>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = VH(InOutStatisticsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(
        vh: VH,
        position: Int
    ) {
        vh.bind(list[position], position == list.size - 1)
    }

    override fun getItemCount() = list.count()

    class VH(val binding: InOutStatisticsItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(result: InOutStatisticsResult, isLast: Boolean) {
            binding.apply {
                root.background =
                    ContextCompat.getDrawable(
                        root.context,
                        if (isLast) R.drawable.bg_round_10_bottom else R.drawable.bg_round_10_middle
                    )
                tv0.text = result.date
                tv1.text = "${result.inCount}"
                tv2.text = "${result.outCount}"
            }
        }
    }

}

class InOutStatisticsHeader : RecyclerView.Adapter<InOutStatisticsHeader.VH>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        p1: Int
    ) = VH(
        AccessRecordListHeaderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(
        vh: VH,
        p1: Int
    ) {
        vh.binding.tvHeaderTitle.text = "每日进出统计"
    }

    override fun getItemCount() = 1

    class VH(val binding: AccessRecordListHeaderBinding) : RecyclerView.ViewHolder(binding.root)

}

class InOutStatisticsTitle : RecyclerView.Adapter<InOutStatisticsTitle.VH>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        p1: Int
    ) = VH(
        InOutStatisticsItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(
        vh: VH,
        p1: Int
    ) {
        vh.binding.apply {
            root.background = ContextCompat.getDrawable(root.context, R.drawable.bg_round_10_top)
            tv0.text = "日期"
            tv1.text = "进入人次"
            tv2.text = "出来人次"
        }
    }

    override fun getItemCount() = 1

    class VH(val binding: InOutStatisticsItemBinding) : RecyclerView.ViewHolder(binding.root)

}