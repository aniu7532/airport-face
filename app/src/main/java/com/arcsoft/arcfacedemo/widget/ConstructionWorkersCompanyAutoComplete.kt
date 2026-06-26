package com.arcsoft.arcfacedemo.widget

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.core.content.withStyledAttributes
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.arcsoft.arcfacedemo.R
import com.arcsoft.arcfacedemo.databinding.ActivityConstructionWorkersCompanyAutocompleteBinding
import com.arcsoft.arcfacedemo.network.CheckUnitRepository
import kotlinx.coroutines.launch

/**
 * 申办单位：可输入 + 联想下拉（与施工人员筛选栏风格一致，兼容 AppCompat 主题）。
 */
class ConstructionWorkersCompanyAutoComplete @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ActivityConstructionWorkersCompanyAutocompleteBinding.inflate(
        LayoutInflater.from(context),
        this,
    )

    private var allNames: List<String> = emptyList()
    private var onCompanyNameChanged: ((String) -> Unit)? = null
    private var suppressCallback = false

    private val autoComplete: AppCompatAutoCompleteTextView
        get() = binding.autoComplete

    private val dropdownAdapter = CompanyNameAdapter(context)

    init {
        orientation = VERTICAL
        autoComplete.setAdapter(dropdownAdapter)
        autoComplete.threshold = 0

        context.withStyledAttributes(attrs, R.styleable.ConstructionWorkersCompanyAutoComplete) {
            val title = getString(R.styleable.ConstructionWorkersCompanyAutoComplete_title)
            if (!title.isNullOrBlank()) {
                binding.title.text = title
            }
            val placeholder = getString(R.styleable.ConstructionWorkersCompanyAutoComplete_placeholder)
            if (!placeholder.isNullOrBlank()) {
                autoComplete.hint = placeholder
            }
        }

        autoComplete.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (suppressCallback) return
                onCompanyNameChanged?.invoke(s?.toString().orEmpty())
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })

        autoComplete.setOnItemClickListener { _, _, position, _ ->
            val name = dropdownAdapter.getItem(position) ?: return@setOnItemClickListener
            setCompanyNameInternal(name)
            autoComplete.dismissDropDown()
            onCompanyNameChanged?.invoke(name)
        }

        autoComplete.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && allNames.isNotEmpty()) {
                autoComplete.post { refreshCompanyFilter() }
            } else if (!hasFocus) {
                autoComplete.dismissDropDown()
            }
        }
    }

    /** 绑定生命周期并拉取申办单位列表，注册名称变化回调。 */
    fun bind(lifecycleOwner: LifecycleOwner, onChanged: (String) -> Unit) {
        onCompanyNameChanged = onChanged
        lifecycleOwner.lifecycleScope.launch {
            allNames = CheckUnitRepository.fetchSimpleList()
                .mapNotNull { it.name?.takeIf(String::isNotEmpty) }
            if (autoComplete.hasFocus()) {
                autoComplete.post { refreshCompanyFilter() }
            }
        }
    }

    /** 设置申办单位名称（不触发重复回调）。 */
    fun setCompanyName(name: String) {
        if (autoComplete.text?.toString() == name) return
        setCompanyNameInternal(name)
    }

    fun clear() {
        setCompanyNameInternal("")
    }

    private fun setCompanyNameInternal(name: String) {
        suppressCallback = true
        autoComplete.setText(name, false)
        suppressCallback = false
    }

    private fun filterCompanyNames(query: String): List<String> =
        if (query.isEmpty()) {
            allNames
        } else {
            allNames.filter { it.contains(query, ignoreCase = true) }
        }

    private fun refreshCompanyFilter() {
        if (allNames.isEmpty()) return
        dropdownAdapter.filter.filter(autoComplete.text)
    }

    /** 包含匹配；仅由 AutoComplete 内部 Filter 触发，避免重复刷新导致下拉闪动。 */
    private inner class CompanyNameAdapter(
        context: Context,
    ) : ArrayAdapter<String>(
        context,
        R.layout.item_company_autocomplete_dropdown,
        android.R.id.text1,
        mutableListOf(),
    ) {
        private val containsFilter: Filter = object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filtered: List<String> = filterCompanyNames(constraint?.toString().orEmpty())
                return FilterResults().apply {
                    values = filtered
                    count = filtered.size
                }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                val list: List<String> = results?.values as? List<String> ?: emptyList()
                replaceItems(list)
            }
        }

        override fun getFilter(): Filter = containsFilter

        fun replaceItems(newItems: List<String>) {
            val current: List<String> = (0 until count).mapNotNull { getItem(it) }
            if (current == newItems) return
            clear()
            addAll(newItems)
            notifyDataSetChanged()
        }
    }
}
