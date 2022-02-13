package com.example.models.requests
import kotlinx.serialization.*

@Serializable
data class Search(val searchTerm: String, val itemIds: List<Int>)