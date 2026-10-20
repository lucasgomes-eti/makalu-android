package eti.lucasgomes.makalu.shared.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = OrderStatusSerializer::class)
enum class OrderStatus {
    PENDING,
    ACCEPTED,
    CANCELLED,
    IN_ROUTE,
    FINISHED,
}

internal object OrderStatusSerializer : KSerializer<OrderStatus> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "eti.lucasgomes.makalu.shared.model.OrderStatus",
        PrimitiveKind.STRING
    )

    override fun deserialize(decoder: Decoder): OrderStatus {
        val value = decoder.decodeString()
        return OrderStatus.entries.firstOrNull { it.name == value } ?: OrderStatus.PENDING
    }

    override fun serialize(encoder: Encoder, value: OrderStatus) {
        encoder.encodeString(value.name)
    }
}
