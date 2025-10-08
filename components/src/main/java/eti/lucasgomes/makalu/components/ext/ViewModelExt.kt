package eti.lucasgomes.makalu.components.ext

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

inline fun ViewModel.withViewModelScope(crossinline block: suspend () -> Unit) {
    viewModelScope.launch { block() }
}