package com.arcsoft.arcfacedemo.ui

import androidx.fragment.app.Fragment
import com.arcsoft.arcfacedemo.widget.ConstructionWorkersCompanyAutoComplete

fun Fragment.bindCompanyUnitField(
    field: ConstructionWorkersCompanyAutoComplete,
    onCompanyNameChanged: (String) -> Unit,
) {
    field.bind(this, onCompanyNameChanged)
}
