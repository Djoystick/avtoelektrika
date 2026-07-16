package com.example.autoelectricai.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoelectricai.data.db.DtcEntity
import com.example.autoelectricai.data.search.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _results = MutableStateFlow<SearchRepository.SearchResult>(
        SearchRepository.SearchResult(emptyList(), emptyList())
    )
    val results: StateFlow<SearchRepository.SearchResult> = _results.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _recentQueries = MutableStateFlow<List<String>>(emptyList())
    val recentQueries: StateFlow<List<String>> = _recentQueries.asStateFlow()

    private val _selectedDtc = MutableStateFlow<DtcEntity?>(null)
    val selectedDtc: StateFlow<DtcEntity?> = _selectedDtc.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
        searchJob?.cancel()

        if (newQuery.length < 2) {
            _results.value = SearchRepository.SearchResult(emptyList(), emptyList())
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)
            _isSearching.value = true
            try {
                _results.value = searchRepository.search(newQuery)
                addRecentQuery(newQuery)
            } catch (e: Exception) {
                _results.value = SearchRepository.SearchResult(emptyList(), emptyList())
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun selectDtc(dtc: DtcEntity) {
        _selectedDtc.value = dtc
    }

    fun clearDtcSelection() {
        _selectedDtc.value = null
    }

    private fun addRecentQuery(query: String) {
        val current = _recentQueries.value.toMutableList()
        current.remove(query)
        current.add(0, query)
        if (current.size > 10) current.removeLast()
        _recentQueries.value = current
    }

    fun clearRecentQueries() {
        _recentQueries.value = emptyList()
    }
}
