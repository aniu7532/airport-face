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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun onResume() {
        super.onResume()
        if (hasResumedOnce) {
            viewModel.search()
        } else {
            hasResumedOnce = true
        }
    }

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