package services.gateway.entity

import java.time.Instant
import java.util.UUID

class Rental(
    val mId: Int,
    val mRentalUid: UUID,
    val mUsername: String,
    val mPaymentUid: UUID,
    val mCarUid: UUID,
    val mDateFrom: Instant,
    val mDateTo: Instant,
    val mStatus: String
)