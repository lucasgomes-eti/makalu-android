package eti.lucasgomes.makalu.features.orders

import eti.lucasgomes.makalu.features.orders.detail.OrderDetailViewModel
import eti.lucasgomes.makalu.features.orders.list.OrdersViewModel
import eti.lucasgomes.makalu.features.orders.tracking.DefaultOrderTrackingLauncher
import eti.lucasgomes.makalu.features.orders.tracking.HiveMqRealtimeClient
import eti.lucasgomes.makalu.features.orders.tracking.OrderNotifications
import eti.lucasgomes.makalu.features.orders.tracking.OrderTracker
import eti.lucasgomes.makalu.features.orders.tracking.OrderTrackingRepository
import eti.lucasgomes.makalu.features.orders.tracking.RealtimeClient
import eti.lucasgomes.makalu.features.orders.tracking.TrackedOrdersStore
import eti.lucasgomes.makalu.shared.tracking.OrderTrackingLauncher
import kotlin.time.Clock
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.binds
import org.koin.dsl.module

val ordersModule = module {
    viewModel { params -> OrderDetailViewModel(params.get(), get(), get()) }
    viewModelOf(::OrdersViewModel)
    singleOf(::OrdersClient)

    // Order tracking
    singleOf(::TrackedOrdersStore)
    single<RealtimeClient> { HiveMqRealtimeClient(get()) }
    single { OrderTracker(get(), get(), get(), Clock.System::now, get()) } binds
        arrayOf(OrderTrackingRepository::class)
    single { OrderNotifications(androidContext()) }
    single<OrderTrackingLauncher> {
        DefaultOrderTrackingLauncher(
            context = androidContext(),
            ordersClient = get(),
            store = get(),
            tracker = get(),
            clock = Clock.System::now,
            logger = get()
        )
    }
}
