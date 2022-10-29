package services.gateway.entity

import java.util.UUID

class Payment(
    val mId: Int,
    val mPaymentUid: UUID,
    val mStatus: String,
    val mPrice: Int
)