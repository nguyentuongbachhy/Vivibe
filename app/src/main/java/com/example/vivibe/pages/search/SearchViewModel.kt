package com.example.vivibe.pages.search

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vivibe.api.song.SongClient
import com.example.vivibe.api.user.UserClient
import com.example.vivibe.manager.FileManager
import com.example.vivibe.manager.UserManager
import com.example.vivibe.model.QuickPicksSong
import com.example.vivibe.model.SongDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel(appContext: Context, private val userManager: UserManager) : ViewModel() {
    private val userClient = UserClient(appContext, userManager.getToken())
    private val fileManager = FileManager(appContext)

    private val _searchResults = MutableStateFlow<List<SongDetail>>(emptyList())
    val searchResults: StateFlow<List<SongDetail>> = _searchResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches

    init {
        loadRecentSearches()
    }

    private fun loadRecentSearches() {
        viewModelScope.launch {
            try {
                val userId = userManager.getId() ?: return@launch
                _recentSearches.value = fileManager.loadFromJson(userId)
                Log.d(TAG, "Loaded recent searches: ${_recentSearches.value}")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading recent searches", e)
            }
        }
    }

    fun searchSongs(query: String) {
        if (query.isEmpty()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "Searching for: $query")

                val results = userClient.search(query)
                Log.d(TAG, "Results: ${results.size}")
                _searchResults.value = results
            } catch (e: Exception) {
                Log.e(TAG, "Error searching songs", e)
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addToRecentSearches(query: String) {
        if (query.isEmpty()) return

        viewModelScope.launch {
            try {
                val userId = userManager.getId() ?: return@launch

                // Thêm vào đầu danh sách, loại bỏ nếu đã tồn tại
                val currentList = _recentSearches.value.toMutableList()
                currentList.remove(query)
                currentList.add(0, query)

                // Giữ tối đa 10 tìm kiếm gần đây
                val updatedList = currentList.take(10)
                _recentSearches.value = updatedList

                // Lưu vào file local
                fileManager.saveToJson(userId, updatedList)
                Log.d(TAG, "Added to recent searches: $query")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding to recent searches", e)
            }
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            try {
                val userId = userManager.getId() ?: return@launch
                _recentSearches.value = emptyList()
                fileManager.clearJson(userId)
                Log.d(TAG, "Cleared recent searches")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing recent searches", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up if needed
    }

    companion object {
        private const val TAG = "SearchViewModel"
    }
}