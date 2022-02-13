package com.example.models.requests

import kotlinx.serialization.*

@Serializable
data class Product(val id: Int, val name: String, val options: List<Option>)

@Serializable
data class Option(val id: Int, val name: String, val price: Double)
