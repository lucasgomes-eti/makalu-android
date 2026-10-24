package eti.lucasgomes.makalu.features.profile

import eti.lucasgomes.makalu.features.profile.model.ProfileResponse
import eti.lucasgomes.makalu.shared.network.MakaluError

fun profileResponse(
    id: Long = 1,
    name: String = "Ana Silva",
    email: String = "ana@makalu.com",
    phoneNumber: String = "11999999999",
    profileImageId: Long? = 7
) = ProfileResponse(
    id = id,
    name = name,
    email = email,
    phoneNumber = phoneNumber,
    profileImageId = profileImageId
)

fun makaluError(
    httpCode: Int = 400,
    message: String = "Bad request",
    internalCode: String = "MK-400"
) = MakaluError(httpCode = httpCode, message = message, internalCode = internalCode)

const val PROFILE_JSON = """
{
  "id": 1,
  "name": "Ana Silva",
  "email": "ana@makalu.com",
  "phone_number": "11999999999",
  "profile_image_id": 5
}
"""

fun makaluErrorJson(httpCode: Int, message: String, internalCode: String) = """
{
  "http_code": $httpCode,
  "message": "$message",
  "internal_code": "$internalCode"
}
"""
