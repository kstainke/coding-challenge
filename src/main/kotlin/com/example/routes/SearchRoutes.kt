package com.example.routes

import com.example.constants.UrlConstants.Companion.baseUrl
import com.example.models.requests.Option
import com.example.models.requests.Product
import com.example.models.requests.Search
import com.example.models.responses.Item
import com.example.models.responses.Meta
import com.example.models.responses.SearchResults
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.text.NumberFormat

fun Application.registerSearchRoutes() {
    routing {
        searchRouting()
    }
}

fun Route.searchRouting() {
    route("/search") {
        get {
            val searchTerm = call.parameters["searchTerm"] ?: return@get call.respondText(
                "Missing or malformed searchTerm parameter",
                status = HttpStatusCode.BadRequest
            )

            // Set up the HTTP client that will pull data from the search APIs
            val client = HttpClient(Apache) {
                // This is required to deserialize the JSON responses from the mock API
                install(JsonFeature) {
                    serializer = KotlinxSerializer()
                    accept(ContentType.Application.Json)
                }
            }

            // Call the search API with the searchTerm parameter
            // This request can be run blocking as subsequent request depend on its results
            val searchRequest: Search = client.get("$baseUrl/search?searchTerm=$searchTerm")

            // Iterate over the returned product IDs and request the product data
            // These calls will be executed in parallel
            val products: MutableList<Product> = mutableListOf()
            coroutineScope {
                searchRequest.itemIds.forEach { productId ->
                    launch {
                        val product = getProduct(client, productId)
                        call.application.log.info(product.toString())
                        products.add(product)
                    }
                }
            } // coroutineScope block will wait here until all child tasks are completed

            // Close the HTTP client as it is no longer needed
            client.close()

            val searchResponse = getSearchResponse(products, searchTerm)

            // Respond with the search results
            call.respond(searchResponse)
        }
    }
}

private fun getSearchResponse(products: List<Product>, searchTerm: String): SearchResults {
    val meta = Meta(searchTerm, products.size)
    val items: MutableList<Item> = mutableListOf()
    for (product in products) {
        val item = Item(product.id, product.name, getProductPriceRange(product.options), product.options)
        items.add(item)
    }

    return SearchResults(meta, items)
}

private fun getProductPriceRange(productOptions: List<Option>): String {
    // Get lowest and highest values
    val lowest = productOptions.minOf { it.price }
    val highest = productOptions.maxOf { it.price }

    // Get currency formatter
    val numberFormat = NumberFormat.getCurrencyInstance()
    numberFormat.maximumFractionDigits = 2

    val lowestFormatted = numberFormat.format(lowest)
    val highestFormatted = numberFormat.format(highest)

    return "$lowestFormatted-$highestFormatted"
}

private suspend fun getProduct(client: HttpClient, productId: Int) : Product {
    return client.get("$baseUrl/product/$productId")
}

