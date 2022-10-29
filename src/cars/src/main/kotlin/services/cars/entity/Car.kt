package services.cars.entity

import java.util.UUID

class Car(
    val mId: Int,
    val mCarUid: UUID,
    val mBrand: String,
    val mModel: String,
    val mRegistrationNumber: String,
    val mPrice: Int,
    val mAvailability: Boolean,
    val mPower: Int? = null,
    val mType: String? = null,
)