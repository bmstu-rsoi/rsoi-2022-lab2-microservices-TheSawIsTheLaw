package services.cars.entity

import java.util.UUID

class Car(
    val mId: Int,
    val mCarUid: UUID,
    val mBrand: String,
    val mModel: String,
    val mRegistrationNumber: String,
    val price: Int,
    val availability: Boolean,
    val power: Int? = null,
    val type: String? = null,
)