package org.example.cmp_shared.store

class HardcodedStoreRepository : StoreRepository {
    override fun getStores(): List<StoreLocation> = listOf(
        StoreLocation(id = 1, name = "江南カフェ", latitude = 37.4979, longitude = 127.0276),
        StoreLocation(id = 2, name = "弘大ベーカリー", latitude = 37.5563, longitude = 126.9237),
        StoreLocation(id = 3, name = "明洞レストラン", latitude = 37.5636, longitude = 126.9826),
        StoreLocation(id = 4, name = "梨泰院バー", latitude = 37.5344, longitude = 126.9944),
    )
}
