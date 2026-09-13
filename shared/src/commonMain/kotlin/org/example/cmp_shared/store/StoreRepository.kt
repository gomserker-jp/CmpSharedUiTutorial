package org.example.cmp_shared.store

interface StoreRepository {
    fun getStores(): List<StoreLocation>
}
