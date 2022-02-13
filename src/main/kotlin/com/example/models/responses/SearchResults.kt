package com.example.models.responses

import com.example.models.requests.Option
import kotlinx.serialization.*

@Serializable
data class SearchResults(val meta: Meta, val items: List<Item>)

@Serializable
data class Meta(val searchTerm: String, val count: Int)

@Serializable
data class Item(val id: Int, val name: String, val priceRange: String, val options: List<Option>)
