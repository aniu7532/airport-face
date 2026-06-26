package com.arcsoft.arcfacedemo.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.arcsoft.arcfacedemo.R
import com.arcsoft.arcfacedemo.databinding.ActivityConstructionWorkersBinding
import com.arcsoft.arcfacedemo.databinding.ActivityConstructionWorkersTabBinding
import com.arcsoft.arcfacedemo.ui.adapter.ConstructionWorkersAdapter
import com.arcsoft.arcfacedemo.ui.viewmodel.ConstructionWorkersTab
import com.arcsoft.arcfacedemo.ui.viewmodel.ConstructionWorkersViewModel
import kotlinx.coroutines.launch

/**
 * 施工人员管理页：顶部 Tab 切换核销记录、通行记录、进出统计三个子页面。
 */
class ConstructionWorkersActivity : AppCompatActivity() {

    private val binding by lazy { ActivityConstructionWorkersBinding.inflate(layoutInflater) }

    private val viewModel by viewModels<ConstructionWorkersViewModel>()

    /** 初始化布局并绑定 Tab 与 ViewPager。 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
        initData()
    }

    /** 配置 ViewPager2、自定义 Tab 栏及 Tab 与页面的双向联动。 */
    fun initView() {
        binding.viewPager.apply {
            isUserInputEnabled = false
            adapter = ConstructionWorkersAdapter(this@ConstructionWorkersActivity)
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    viewModel.changeTab(position)
                }
            })
        }

        val tabBar = binding.tabBar
        val tabs = buildList {
            ConstructionWorkersTab.entries.forEachIndexed { index, type ->
                val tab =
                    ActivityConstructionWorkersTabBinding.inflate(layoutInflater, tabBar, false)
                tab.root.text = type.label
                tab.root.setOnClickListener {
                    viewModel.changeTab(index)
                }
                add(tab)
                tabBar.addView(tab.root)
            }
        }

        lifecycleScope.launch {
            viewModel.currentTab
                .collect { currentTab ->
                    binding.viewPager.setCurrentItem(currentTab.ordinal, false)
                    tabs.forEachIndexed { index, tab ->
                        tab.root.background = if (index == currentTab.ordinal) ContextCompat.getDrawable(this@ConstructionWorkersActivity, R.mipmap.loading_bg) else null
                    }
                }
        }
    }

    /** 预留数据初始化入口，当前无额外逻辑。 */
    fun initData() {

    }

}
