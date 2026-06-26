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
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arcsoft.arcfacedemo.databinding.FragmentWriteOffRecordBinding
import com.arcsoft.arcfacedemo.ui.adapter.WriteOffRecordAdapter
import com.arcsoft.arcfacedemo.ui.adapter.WriteOffRecordHeaderAdapter
import com.arcsoft.arcfacedemo.ui.bindCompanyUnitField
import com.arcsoft.arcfacedemo.ui.viewmodel.WriteOffRecordViewModel
import com.arcsoft.arcfacedemo.widget.dialog.LoadingPopDialog
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 核销记录（有进无出）列表页：支持多条件筛选、分页加载及单条记录核实操作。
 */
class WriteOffRecordFragment : Fragment() {

    companion object {
        fun newInstance() = WriteOffRecordFragment()
    }

    private val binding by lazy { FragmentWriteOffRecordBinding.inflate(layoutInflater) }

    private val viewModel: WriteOffRecordViewModel by viewModels()

    /** 首次 onResume 已由 ViewModel 初始 paging 加载，避免重复请求；之后每次回到本页再 search。 */
    private var hasResumedOnce = false

    private val headerAdapter by lazy {
        WriteOffRecordHeaderAdapter {
            showLoadingPopup()
            adapter.refresh()
        }
    }

    private val adapter by lazy { WriteOffRecordAdapter() }

    /** 创建并返回 Fragment 根视图。 */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    /** 初始化筛选条件、分页列表及加载状态监听。 */
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

    /** 绑定筛选控件、分页列表、总数展示及刷新加载弹窗。 */
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
                        if (it.isEmpty()) {
                            binding.inputName.clear()
                        }
                    }
                }
                launch {
                    viewModel.cardNo.collect {
                        if (it.isEmpty()) {
                            binding.inputCardNo.clear()
                        }
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
                launch {
                    viewModel.listTotal.collect { headerAdapter.setTotal(it) }
                }
                launch {
                    adapter.loadStateFlow.collectLatest { loadStates ->
                        when (loadStates.refresh) {
                            is LoadState.Loading -> {}
                            is LoadState.NotLoading, is LoadState.Error -> dismissLoadingPopup()
                        }
                    }
                }
                viewModel.cardRecords.collect {
                    adapter.submitData(it)
                }
            }
        }
    }

    /** 分页 refresh 会多次 show/dismiss，不可对同一实例设 isDestroyOnDismiss(true) */
    private var loadingPopup: BasePopupView? = null

    private fun showLoadingPopup() {
        if (!isAdded) return
        val popup = loadingPopup ?: XPopup.Builder(requireActivity())
            .dismissOnBackPressed(false)
            .dismissOnTouchOutside(false)
            .asCustom(LoadingPopDialog(requireActivity(), "加载中，请稍后......"))
            .also { loadingPopup = it }
        if (!popup.isShow) {
            popup.show()
        }
    }

    private fun dismissLoadingPopup() {
        loadingPopup?.takeIf { it.isShow }?.dismiss()
    }

    /** 销毁视图时关闭加载弹窗，避免内存泄漏。 */
    override fun onDestroyView() {
        dismissLoadingPopup()
        loadingPopup = null
        super.onDestroyView()
    }
}