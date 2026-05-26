package com.arcsoft.arcfacedemo.ui

import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.arcsoft.arcfacedemo.network.CheckUnitRepository
import com.arcsoft.arcfacedemo.widget.ConstructionWorkersSelector
import com.arcsoft.arcfacedemo.widget.dialog.StringListPickerDialog
import com.blankj.utilcode.util.ToastUtils
import kotlinx.coroutines.launch

fun Fragment.bindCompanyUnitSelector(
    selector: ConstructionWorkersSelector,
    onSelected: (companyName: String) -> Unit,
) {
    selector.setOnClickListener {
        lifecycleScope.launch {
            val units = CheckUnitRepository.fetchSimpleList()
            val names = units.mapNotNull { it.name?.takeIf(String::isNotEmpty) }
            if (names.isEmpty()) {
                ToastUtils.showShort("暂无申办单位数据")
                return@launch
            }
            StringListPickerDialog.show(
                requireContext(),
                "请选择申办单位",
                names,
                { _, text ->
                    onSelected(text)
                    selector.setValue(text)
                },
                null,
            )
        }
    }
}
