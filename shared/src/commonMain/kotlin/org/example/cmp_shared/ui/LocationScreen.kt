package org.example.cmp_shared.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.cmp_shared.location.Coordinates
import org.example.cmp_shared.store.StoreLocation

private fun formatCoordinate(value: Double): String {
    val scaled = kotlin.math.round(value * 1_000_000.0) / 1_000_000.0
    return scaled.toString()
}

@Composable
fun LocationScreen(
    viewModel: LocationViewModel = viewModel { LocationViewModel() },
) {
    val uiState = viewModel.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        UserLocationSection(
            state = uiState.userLocationState,
            onRetry = viewModel::loadLocation,
        )

        SortOrderSection(
            sortOrder = uiState.sortOrder,
            onSortOrderSelected = viewModel::setSortOrder,
        )

        StoreList(stores = uiState.stores)
    }
}

@Composable
private fun UserLocationSection(
    state: UserLocationState,
    onRetry: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "現在地",
            style = MaterialTheme.typography.titleMedium,
        )

        when (state) {
            UserLocationState.Loading -> Text(
                text = "位置情報を取得中...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            is UserLocationState.Success -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "緯度: ${formatCoordinate(state.coordinates.latitude)}",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "経度: ${formatCoordinate(state.coordinates.longitude)}",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            UserLocationState.PermissionDenied -> LocationErrorSection(
                message = "位置情報の権限が必要です",
                isError = true,
                onRetry = onRetry,
            )
            UserLocationState.Unavailable -> LocationErrorSection(
                message = "位置情報を取得できませんでした",
                isError = false,
                onRetry = onRetry,
            )
        }
    }
}

@Composable
private fun LocationErrorSection(
    message: String,
    isError: Boolean,
    onRetry: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        TextButton(onClick = onRetry) {
            Text("再取得")
        }
    }
}

@Composable
private fun SortOrderSection(
    sortOrder: SortOrder,
    onSortOrderSelected: (SortOrder) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = sortOrder == SortOrder.ASC,
            onClick = { onSortOrderSelected(SortOrder.ASC) },
            label = { Text("近い順") },
        )
        FilterChip(
            selected = sortOrder == SortOrder.DESC,
            onClick = { onSortOrderSelected(SortOrder.DESC) },
            label = { Text("遠い順") },
        )
    }
}

@Composable
private fun StoreList(stores: List<StoreLocation>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        items(stores, key = { it.id }) { store ->
            StoreListRow(store = store)
            HorizontalDivider()
        }
    }
}

@Composable
private fun StoreListRow(store: StoreLocation) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = store.name,
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            text = "${formatCoordinate(store.latitude)}, ${formatCoordinate(store.longitude)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun LocationScreenPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            UserLocationSection(
                state = UserLocationState.Success(
                    coordinates = Coordinates(latitude = 37.566535, longitude = 126.977969),
                ),
                onRetry = {},
            )
            SortOrderSection(
                sortOrder = SortOrder.ASC,
                onSortOrderSelected = {},
            )
            StoreList(
                stores = listOf(
                    StoreLocation(id = 1, name = "江南カフェ", latitude = 37.4979, longitude = 127.0276),
                    StoreLocation(id = 2, name = "弘大ベーカリー", latitude = 37.5563, longitude = 126.9237),
                ),
            )
        }
    }
}
