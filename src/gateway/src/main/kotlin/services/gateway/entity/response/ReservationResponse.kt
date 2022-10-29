package services.gateway.entity.response

import services.gateway.entity.Payment
import java.time.Instant
import java.util.*

class ReservationResponse(
    val rentalUid: UUID,
    val status: String,
    val carUid: UUID,
    val dateFrom: Instant,
    val dateTo: Instant,
    val payment: Payment
)