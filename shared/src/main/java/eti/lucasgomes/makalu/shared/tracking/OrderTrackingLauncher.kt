package eti.lucasgomes.makalu.shared.tracking

interface OrderTrackingLauncher {
    fun onOrderPlaced(orderId: Long)

    suspend fun onAppOpened()

    companion object {
        const val EXTRA_ORDER_ID = "eti.lucasgomes.makalu.extra.ORDER_ID"
    }
}
