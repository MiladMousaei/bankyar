package com.bankyar.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bankyar.data.DarkModeManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(app: Application) : AndroidViewModel(app) {
    private val manager = DarkModeManager(app)
    val isDarkMode = manager.isDarkMode.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    fun toggle() = viewModelScope.launch { manager.toggle() }
}
