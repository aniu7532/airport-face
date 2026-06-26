package com.arcsoft.arcfacedemo.ui.fragment

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arcsoft.arcfacedemo.databinding.FragmentAccessRecordBinding
import com.arcsoft.arcfacedemo.ui.adapter.AccessRecordAdapter
import com.arcsoft.arcfacedemo.ui.adapter.AccessRecordHeaderAdapter
import com.arcsoft.arcfacedemo.ui.bindCompanyUnitField
import com.arcsoft.arcfacedemo.ui.viewmodel.AccessRecordViewModel
import kotlinx.coroutines.launch

/**
 * 通行记录列表页：支持按姓名、证件号、时间、申办单位筛选，分页展示查验通行记录。
 */
class AccessRecordFragment : Fragment() {

    companion object {
        fun newInstance() = AccessRecordFragment()
    }

    private val binding by lazy { FragmentAccessRecordBinding.inflate(layoutInflater) }

    private val viewModel: AccessRecordViewModel by viewModels()

    /** 首次 onResume 已由 ViewModel 初始 paging 加载；之后每次回到本页再 search。 */
    private var hasResumedOnce = false

    private val headerAdapter by lazy { AccessRecordHeaderAdapter() }

    private val adapter by lazy { AccessRecordAdapter() }

    /** 创建并返回 Fragment 根视图。 */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root

    /** 初始化筛选条件、列表及数据流订阅。 */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    /** 非首次回到页面时自动刷新列表数据。 */
    override fun onResume() {
        super.onResume()
        if (hasResumedOnce) {
            viewModel.search()
        } else {
            hasResumedOnce = true
        }
    }

    /** 绑定筛选控件、分页列表及 ViewModel 状态同步。 */
    fun initView() {
        binding.apply {
            inputName.addTextChangedListener {
                viewModel.setName(it)
            }
            inputCardNo.addTextChangedListener {
                viewModel.setCardNo(it)
            }
            selectorStartTime.addOnTimeChangedListener {
                viewModel.setStartTime(it)
            }
            selectorEndTime.addOnTimeChangedListener {
                viewModel.setEndTime(it)
            }
            bindCompanyUnitField(selectorCompany) { viewModel.setCompanyName(it) }
            foldView.setOnFoldListener {
                foldGroup.visibility = if (it) View.GONE else View.VISIBLE
            }
            btnReset.setOnClickListener {
                viewModel.reset()
            }
            btnSearch.setOnClickListener {
                viewModel.search()
            }
            listView.adapter = adapter.withListHeader(headerAdapter)
            listView.layoutManager = LinearLayoutManager(
                context, LinearLayoutManager.VERTICAL, false
            )
            listView.addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val pos = parent.getChildAdapterPosition(view)
                    if (pos == RecyclerView.NO_POSITION) return
                    // position 0 为列表 header，间距略小
                    outRect.bottom = if (pos == 0) 8 else 20
                }
            })
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.name.collect {
                        if (it.isEmpty()) { binding.inputName.clear() }
                    }
                }
                launch {
                    viewModel.cardNo.collect {
                        if (it.isEmpty()) { binding.inputCardNo.clear() }
                    }
                }
                launch {
                    viewModel.startTime.collect {
                        binding.selectorStartTime.setValue(it)
                    }
                }
                launch {
                    viewModel.endTime.collect {
                        binding.selectorEndTime.setValue(it)
                    }
                }
                launch {
                    viewModel.companyName.collect {
                        binding.selectorCompany.setCompanyName(it)
                    }
                }
                viewModel.cardRecords.collect {
                    adapter.submitData(it)
                }
            }
        }
    }

}