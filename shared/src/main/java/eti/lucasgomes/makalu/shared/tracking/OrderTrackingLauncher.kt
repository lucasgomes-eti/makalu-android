package eti.lucasgomes.makalu.shared.tracking

/**
 * Starts live tracking of the user's orders. Both calls must be made while the app is
 * in the foreground: that is the only time Android lets it start a foreground service.
 */
interface OrderTrackingLauncher {

    /** The order was just created successfully. */
    fun onOrderPlaced(orderId: Long)

    /** The app came to the foreground: resume tracking if an order is still in progress. */
    suspend fun onAppOpened()

    companion object {
        /** Intent extra carrying the id of the order a notification was about. */
        const val EXTRA_ORDER_ID = "eti.lucasgomes.makalu.extra.ORDER_ID"
    }
}
