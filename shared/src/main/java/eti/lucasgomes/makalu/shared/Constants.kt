package eti.lucasgomes.makalu.shared

const val IMAGE_TEMP_FILE_SUFFIX = ".jpg"
const val FILE_PROVIDER_AUTHORITY = "eti.lucasgomes.makalu.provider"
val REGEX_EMAIL = "^[\\w-]+@([\\w-]+\\.)+[\\w-]{2,4}$".toRegex()
val REGEX_PASSWORD = "^(?=.*[A-Za-z])(?=.*\\d).+$".toRegex()

const val MAX_PASSWORD_LENGTH = 25

const val MIN_PASSWORD_LENGTH = 8

const val PROFILE_PIC_TEMP_FILE_PREFIX = "profile_pic_"