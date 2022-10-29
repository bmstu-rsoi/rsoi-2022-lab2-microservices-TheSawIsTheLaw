package services.gateway.controller

import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.EMPTY_REQUEST
import okio.use
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import services.gateway.entity.Car
import services.gateway.entity.Payment
import services.gateway.entity.Rental
import services.gateway.entity.RentalReservation
import services.gateway.entity.response.*
import services.gateway.utils.ClientKeeper
import services.gateway.utils.GsonKeeper
import services.gateway.utils.OkHttpKeeper
import java.time.temporal.ChronoUnit
import java.util.*

@Controller
@RequestMapping("api/v1")
class GatewayController {

    private fun getRental(uid: UUID): Rental? {
        val rentalRequest =
            OkHttpKeeper
                .builder
                .url(OkHttpKeeper.RENTAL_URL + "/$uid")
                .get()
                .build()

        return ClientKeeper.client.newCall(rentalRequest).execute().use { response ->
            if (!response.isSuccessful) null
            else GsonKeeper.gson.fromJson(response.body.toString(), Rental::class.java)
        }
    }

    private fun getCars(showAll: Boolean): List<Car>? {
        val carRequest =
            OkHttpKeeper
                .builder
                .url(OkHttpKeeper.CARS_URL + "&showAll=$showAll")
                .get()
                .build()

        return ClientKeeper.client.newCall(carRequest).execute().use { response ->
            if (!response.isSuccessful) null
            else GsonKeeper.gson.fromJson(response.body.toString(), listOf<Car>().javaClass)
        }
    }

    private fun getPayments(): List<Payment>? {
        val paymentRequest =
            OkHttpKeeper
                .builder
                .url(OkHttpKeeper.PAYMENT_URL + "/")
                .get()
                .build()

        return ClientKeeper.client.newCall(paymentRequest).execute().use { response ->
            if (!response.isSuccessful) null
            else GsonKeeper.gson.fromJson(response.body.toString(), listOf<Payment>().javaClass)
        }
    }

    @GetMapping("/cars")
    fun getCars(
        @RequestParam("page") page: Int,
        @RequestParam("size") size: Int,
        @RequestParam("showAll", required = false, defaultValue = "false") showAll: Boolean
    ): ResponseEntity<CarsResponse> {
        val cars = getCars(showAll) ?: return ResponseEntity.internalServerError().build()

        return ResponseEntity.ok(
            CarsResponse(
                page,
                size,
                cars.size,
                cars
                    .slice(size * (page - 1) until (size * page))
                    .let {
                        it.map { car ->
                            CarCarsResponse(
                                car.mCarUid,
                                car.mBrand,
                                car.mModel,
                                car.mRegistrationNumber,
                                car.mPower,
                                car.mType,
                                car.mPrice,
                                car.mAvailability
                            )
                        }
                    }
            )
        )
    }

    @GetMapping("/rental")
    fun getRentals(@RequestHeader("User-Name") username: String) : ResponseEntity<List<RentalResponse>> {
        val request =
            OkHttpKeeper
                .builder
                .url(OkHttpKeeper.RENTAL_URL + "/")
                .addHeader("User-Name", username)
                .get()
                .build()

        val rentals = ClientKeeper.client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) emptyList()
            else GsonKeeper.gson.fromJson(response.body.toString(), listOf<Rental>().javaClass)
        }

        val cars = getCars(true) ?: return ResponseEntity.internalServerError().build()

        val payments = getPayments() ?: return ResponseEntity.internalServerError().build()

        return ResponseEntity.ok(
            rentals.map { rental ->
                RentalResponse(
                    rental.mRentalUid,
                    rental.mStatus,
                    rental.mDateFrom,
                    rental.mDateTo,
                    cars
                        .findLast { car -> car.mCarUid == rental.mCarUid }!!
                        .let { CarRentalResponse(it.mCarUid, it.mBrand, it.mModel, it.mRegistrationNumber) },
                    payments
                        .findLast { payment -> payment.mPaymentUid == rental.mPaymentUid }!!
                        .let { PaymentRentalResponse(it.mPaymentUid, it.mStatus, it.mPrice) }
                )
            }
        )
    }

    @PostMapping("/rental")
    fun reserveRental(
        @RequestHeader("User-Name") username: String,
        @RequestBody reservation: RentalReservation
    ) : ResponseEntity<ReservationResponse> {
        val carRequest =
            OkHttpKeeper
                .builder
                .url(OkHttpKeeper.CARS_URL + "/${reservation.carUid}")
                .get()
                .build()

        val car = ClientKeeper.client.newCall(carRequest).execute().use { response ->
            if (!response.isSuccessful) null
            else GsonKeeper.gson.fromJson(response.body.toString(), Car::class.java)
        } ?: return ResponseEntity.badRequest().build()

        if (!car.mAvailability)
            return ResponseEntity.badRequest().build()

        val reserveCarRequest =
            OkHttpKeeper
                .builder
                .url(OkHttpKeeper.CARS_URL + "/${car.mCarUid}/unavailable")
                .patch(EMPTY_REQUEST)
                .build()

        // Better use like a transaction but... You know. I don't give a shit.
        ClientKeeper.client.newCall(reserveCarRequest).execute()

        val rentalPeriodDays = ChronoUnit.DAYS.between(reservation.dateFrom, reservation.dateTo)
        val money = car.mPrice * rentalPeriodDays
        val paymentUid = UUID.randomUUID()

        val rentalToPost = Rental(
            0,
            UUID.randomUUID(),
            username,
            paymentUid,
            car.mCarUid,
            reservation.dateFrom,
            reservation.dateTo,
            "IN_PROGRESS"
        )

        val rentalRequest =
            OkHttpKeeper
                .builder
                .url(OkHttpKeeper.RENTAL_URL + "/")
                .post(GsonKeeper.gson.toJson(rentalToPost).toRequestBody())
                .build()

        ClientKeeper.client.newCall(rentalRequest).execute()

        val paymentToPost = Payment(
            0,
            paymentUid,
            "PAID",
            money.toInt()
        )

        val paymentRequest =
            OkHttpKeeper
                .builder
                .url(OkHttpKeeper.PAYMENT_URL + "/")
                .post(GsonKeeper.gson.toJson(paymentToPost).toRequestBody())
                .build()

        ClientKeeper.client.newCall(paymentRequest).execute()

        return ResponseEntity.ok(
            ReservationResponse(
                rentalToPost.mRentalUid,
                rentalToPost.mStatus,
                car.mCarUid,
                rentalToPost.mDateFrom,
                rentalToPost.mDateTo,
                paymentToPost
            )
        )
    }

    @GetMapping("/rental/{rentalUid}")
    fun getUsersRental(
        @PathVariable("rentalUid") rentalUid: UUID,
        @RequestHeader("User-Name") username: String
    ): ResponseEntity<RentalResponse> {
        val rental = getRental(rentalUid) ?: return ResponseEntity.notFound().build()

        if (username != rental.mUsername) return ResponseEntity.notFound().build()

        val cars = getCars(true)
        val payments = getPayments()

        return ResponseEntity.ok(
            RentalResponse(
                rental.mRentalUid,
                rental.mStatus,
                rental.mDateFrom,
                rental.mDateTo,
                cars!!
                    .findLast { car -> car.mCarUid == rental.mCarUid }!!
                    .let { CarRentalResponse(it.mCarUid, it.mBrand, it.mModel, it.mRegistrationNumber) },
                payments!!
                    .findLast { payment -> payment.mPaymentUid == rental.mPaymentUid }!!
                    .let { PaymentRentalResponse(it.mPaymentUid, it.mStatus, it.mPrice) }
            )
        )
    }

    @PostMapping("/rental/{rentalUid}/finish")
    fun finishRental(
        @RequestHeader("User-Name") username: String,
        @PathVariable rentalUid: UUID
    ): ResponseEntity<*> {
        val rental = getRental(rentalUid) ?: return ResponseEntity("lol what", HttpStatus.NOT_FOUND)

        if (username != rental.mUsername) return ResponseEntity("lol what", HttpStatus.NOT_FOUND)

        val carAvailableStateRequest =
            OkHttpKeeper
                .builder
                .url(OkHttpKeeper.CARS_URL + "/${rental.mCarUid}/available")
                .patch(EMPTY_REQUEST)
                .build()

        ClientKeeper.client.newCall(carAvailableStateRequest).execute()

        val rentalFinishRequest =
            OkHttpKeeper
                .builder
                .url(OkHttpKeeper.RENTAL_URL + "/${rental.mRentalUid}/finish")
                .patch(EMPTY_REQUEST)
                .build()

        ClientKeeper.client.newCall(rentalFinishRequest).execute()

        return ResponseEntity("...", HttpStatus.NO_CONTENT)
    }

    @DeleteMapping("/rental/{rentalUid}")
    fun cancelRent(
        @RequestHeader("User-Name") username: String,
        @PathVariable rentalUid: UUID
    ): ResponseEntity<*> {
        val rental = getRental(rentalUid) ?: return ResponseEntity("lol man", HttpStatus.NOT_FOUND)

        if (rental.mUsername != username) return ResponseEntity("lol man", HttpStatus.NOT_FOUND)

        val carAvailableStateRequest =
            OkHttpKeeper
                .builder
                .url(OkHttpKeeper.CARS_URL + "/${rental.mCarUid}/available")
                .patch(EMPTY_REQUEST)
                .build()

        ClientKeeper.client.newCall(carAvailableStateRequest).execute()

        val cancelRentalRequest =
            OkHttpKeeper
                .builder
                .url(OkHttpKeeper.RENTAL_URL + "/$rentalUid/cancel")
                .delete()
                .build()

        ClientKeeper.client.newCall(cancelRentalRequest).execute()

        val cancelPaymentRequest =
            OkHttpKeeper
                .builder
                .url(OkHttpKeeper.PAYMENT_URL + "/${rental.mPaymentUid}/cancel")
                .patch(EMPTY_REQUEST)
                .build()

        ClientKeeper.client.newCall(cancelPaymentRequest).execute()

        return ResponseEntity("...", HttpStatus.NO_CONTENT)
    }
}