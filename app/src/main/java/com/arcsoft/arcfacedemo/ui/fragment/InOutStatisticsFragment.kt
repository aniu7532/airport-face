package com.arcsoft.arcfacedemo.ui.fragment

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arcsoft.arcfacedemo.R
import com.arcsoft.arcfacedemo.ui.viewmodel.InOutStatisticsViewModel

class InOutStatisticsFragment : Fragment() {

    companion object {
        fun newInstance() = InOutStatisticsFragment()
    }

    private val viewModel: InOutStatisticsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_in_out_statistics, container, false)
    }
}