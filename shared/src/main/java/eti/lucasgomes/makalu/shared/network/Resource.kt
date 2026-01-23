package eti.lucasgomes.makalu.shared.network

sealed interface Resource<out D> {
    data class Success<out D>(val data: D) : Resource<D>
    data class Error(val error: MakaluError) : Resource<Nothing>
}

fun unexpectedErrorWithException(e: Exception) =
    MakaluError(
        httpCode = -1,
        message = "Unexpected error during request. Cause: ${e.message}",
        internalCode = "MK-0",
    )

fun unexpectedErrorWithHttpStatusCode(statusCode: Int) =
    MakaluError(
        httpCode = statusCode,
        message = "Unexpected http error. Code: $statusCode",
        internalCode = "MK-0",
    )

inline fun <T, R> Resource<T>.map(map: (T) -> R): Resource<R> {
    return when (this) {
        is Resource.Error -> Resource.Error(error)
        is Resource.Success -> Resource.Success(map(data))
    }
}

fun <T> Resource<T>.asEmptyDataResult(): EmptyResult {
    return map { }
}

inline fun <T> Resource<T>.onSuccess(action: (T) -> Unit): Resource<T> {
    return when (this) {
        is Resource.Error -> this
        is Resource.Success -> {
            action(data)
            this
        }
    }
}

inline fun <T> Resource<T>.onError(action: (MakaluError) -> Unit): Resource<T> {
    return when (this) {
        is Resource.Error -> {
            action(error)
            this
        }

        is Resource.Success -> this
    }
}

typealias EmptyResult = Resource<Unit>