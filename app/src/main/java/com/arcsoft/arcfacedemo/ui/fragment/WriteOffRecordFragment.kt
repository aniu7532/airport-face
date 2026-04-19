package com.arcsoft.arcfacedemo.ui.fragment

import android.graphics.Rect
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arcsoft.arcfacedemo.databinding.FragmentWriteOffRecordBinding
import com.arcsoft.arcfacedemo.ui.adapter.WriteOffRecordAdapter
import com.arcsoft.arcfacedemo.ui.adapter.WriteOffRecordHeaderAdapter
import com.arcsoft.arcfacedemo.ui.viewmodel.WriteOffRecordViewModel
import kotlinx.coroutines.launch

class WriteOffRecordFragment : Fragment() {

    companion object {
        fun newInstance() = WriteOffRecordFragment()
    }

    private val binding by lazy { FragmentWriteOffRecordBinding.inflate(layoutInflater) }

    private val viewModel: WriteOffRecordViewModel by viewModels()

    private val headerAdapter by lazy { WriteOffRecordHeaderAdapter() }

    private val adapter by lazy { WriteOffRecordAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return binding.root
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
                        if (it == null) { binding.selectorStartTime.clear() }
                    }
                }
                launch {
                    viewModel.endTime.collect {
                        if (it == null) { binding.selectorEndTime.clear() }
                    }
                }
                launch {
                    viewModel.listTotal.collect { headerAdapter.setTotal(it) }
                }
                viewModel.cardRecords.collect {
                    adapter.submitData(it)
                }
            }
        }
    }
}