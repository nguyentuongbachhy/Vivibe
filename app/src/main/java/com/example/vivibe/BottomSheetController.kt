package com.example.vivibe

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BottomSheetController {
    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet: StateFlow<Boolean> = _showBottomSheet

    private val _currentSong = MutableStateFlow<QuickPicksSong?>(null)
    val currentSong: StateFlow<QuickPicksSong?> = _currentSong

    fun showBottomSheet(song: QuickPicksSong) {
        _currentSong.value = song
        _showBottomSheet.value = true
    }

    fun hideBottomSheet() {
        _showBottomSheet.value = false
        _currentSong.value = null
    }
}