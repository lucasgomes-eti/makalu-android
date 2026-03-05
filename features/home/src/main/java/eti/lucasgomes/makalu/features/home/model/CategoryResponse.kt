package eti.lucasgomes.makalu.features.home.model

import kotlinx.serialization.Serializable

@Serializable
data class CategoryResponse(val id: Long, val description: String)