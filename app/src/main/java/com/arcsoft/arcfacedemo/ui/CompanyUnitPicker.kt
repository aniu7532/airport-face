package com.arcsoft.arcfacedemo.ui

import androidx.fragment.app.Fragment
import com.arcsoft.arcfacedemo.widget.ConstructionWorkersCompanyAutoComplete

/**
 * 在 Fragment 中绑定申办单位联想输入框，拉取单位列表并监听名称变化。
 */
fun Fragment.bindCompanyUnitField(
    field: ConstructionWorkersCompanyAutoComplete,
    onCompanyNameChanged: (String) -> Unit,
) {
    field.bind(this, onCompanyNameChanged)
}
