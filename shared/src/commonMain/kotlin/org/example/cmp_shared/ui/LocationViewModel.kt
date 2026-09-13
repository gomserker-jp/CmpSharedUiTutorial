package org.example.cmp_shared.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.example.cmp_shared.location.Coordinates
import org.example.cmp_shared.location.LocationProvider
import org.example.cmp_shared.location.createLocationProvider
import org.example.cmp_shared.store.HardcodedStoreRepository
import org.example.cmp_shared.store.StoreLocation
import org.example.cmp_shared.store.StoreRepository
import org.example.cmp_shared.util.DistanceCalculator

enum class SortOrder {
    ASC,
    DESC,
}

data class LocationUiState(
    val userLocationState: UserLocationState = UserLocationState.Loading,
    val stores: List<StoreLocation> = emptyList(),
    val sortOrder: SortOrder = SortOrder.ASC,
)

class LocationViewModel(
    private val locationProvider: LocationProvider = createLocationProvider(),
    private val storeRepository: StoreRepository = HardcodedStoreRepository(),
) : ViewModel() {

    var uiState by mutableStateOf(LocationUiState())
        private set

    init {
        loadLocation()
    }

    fun loadLocation() {
        viewModelScope.launch {
            val sortOrder = uiState.sortOrder
            uiState = uiState.copy(userLocationState = UserLocationState.Loading)
            val result = locationProvider.getCurrentLocation()
            val userLocationState = result.toUserLocationState()
            uiState = uiState.copy(
                userLocationState = userLocationState,
                stores = sortStores(userLocationState.coordinatesOrNull, storeRepository.getStores(), sortOrder),
            )
        }
    }


    fun setSortOrder(sortOrder: SortOrder) {
        if (uiState.sortOrder == sortOrder) return
        uiState = uiState.copy(
            sortOrder = sortOrder,
            stores = sortStores(uiState.userLocationState.coordinatesOrNull, storeRepository.getStores(), sortOrder),
        )
    }

    private fun sortStores(
        userLocation: Coordinates?,
        stores: List<StoreLocation>,
        sortOrder: SortOrder,
    ): List<StoreLocation> {
        if (userLocation == null) return stores

        val comparator = compareBy<StoreLocation> { store ->
            DistanceCalculator.distanceMeters(
                userLocation,
                Coordinates(store.latitude, store.longitude),
            )
        }

        return when (sortOrder) {
            SortOrder.ASC -> stores.sortedWith(comparator)
            SortOrder.DESC -> stores.sortedWith(comparator.reversed())
        }
    }
}
