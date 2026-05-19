package eti.lucasgomes.makalu.shared

import MakaluConfig


fun mapImageUrl(imageId: Long?): String? {
    return imageId?.let { "${MakaluConfig.BASE_URL}images/$imageId" }
}