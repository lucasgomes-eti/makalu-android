package eti.lucasgomes.makalu.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class CategoryResponse(val id: Long, val description: String)