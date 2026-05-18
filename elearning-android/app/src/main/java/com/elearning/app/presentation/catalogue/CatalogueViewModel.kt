package com.elearning.app.presentation.catalogue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.elearning.app.data.remote.api.ResourceApiService
import com.elearning.app.data.remote.paging.FormationPagingSource
import com.elearning.app.domain.model.Formation
import com.elearning.app.domain.model.FormationLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class CatalogueViewModel @Inject constructor(
    private val api: ResourceApiService
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedLevel = MutableStateFlow<FormationLevel?>(null)
    val selectedLevel = _selectedLevel.asStateFlow()

    // Flow that reactively fetches data from PagingSource whenever filters change
    val formationsFlow: Flow<PagingData<Formation>> = combine(
        _searchQuery.debounce(300), // Prevent network spam
        _selectedLevel
    ) { query, level ->
        Pair(query, level)
    }.flatMapLatest { (query, level) ->
        Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = { FormationPagingSource(api, query, level) }
        ).flow
    }.cachedIn(viewModelScope)

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateLevelFilter(level: FormationLevel?) {
        _selectedLevel.value = if (_selectedLevel.value == level) null else level
    }
}
