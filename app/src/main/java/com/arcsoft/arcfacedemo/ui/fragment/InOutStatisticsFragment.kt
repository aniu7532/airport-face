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
import com.arcsoft.arcfacedemo.databinding.FragmentInOutStatisticsBinding
import com.arcsoft.arcfacedemo.ui.adapter.InOutStatisticsAdapter
import com.arcsoft.arcfacedemo.ui.viewmodel.InOutStatisticsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class InOutStatisticsFragment : Fragment() {

    companion object {
        fun newInstance() = InOutStatisticsFragment()
    }

    private val binding by lazy { FragmentInOutStatisticsBinding.inflate(layoutInflater) }

    private val viewModel: InOutStatisticsViewModel by viewModels()

    private val adapter by lazy { InOutStatisticsAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root

    fun initView() {
        binding.apply {
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
                viewModel.request()
            }
            listView.adapter = adapter.withListHeader()
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
                    outRect.bottom = if (pos == 0) 10 else if (pos == 1) 0 else 1
                }
            })
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
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
                viewModel.list.collectLatest {
                    adapter.setList(it)
                }
            }
        }

        viewModel.request()
    }
}