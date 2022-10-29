package services.gateway.controller

import okhttp3.OkHttp
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import services.gateway.entity.Car
import services.gateway.entity.Rental
import services.gateway.entity.RentalReservation
import services.gateway.utils.ClientKeeper
import services.gateway.utils.GsonKeeper
import services.gateway.utils.OkHttpKeeper
import java.util.*

@Controller
@RequestMapping("api/v1")
class GatewayController {

    @GetMapping("/cars")
    fun getCars(
        @RequestParam("page") page: Int,
        @RequestParam("size") size: Int,
        @RequestParam("showAll") showAll: Boolean
    ): ResponseEntity<List<Car>> {
        val request =
            OkHttpKeeper
                .builder
                .url(OkHttpKeeper.CARS_URL + "&showAll=$showAll")
                .get()
                .build()

        val cars = ClientKeeper.client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) emptyList()
            else GsonKeeper.gson.fromJson(response.body.toString(), listOf<Car>().javaClass)
        }

        return ResponseEntity.ok(cars.slice(size * (page - 1) until (size * page)))
    }

    @GetMapping("/rental")
    fun getRentals(@RequestHeader("User-Name") username: String) : ResponseEntity<List<Rental>> {
        val request =
            OkHttpKeeper
                .builder
                .url(OkHttpKeeper.RENTAL_URL)
                .addHeader("User-Name", username)
                .get()
                .build()

        val rentals = ClientKeeper.client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) emptyList()
            else GsonKeeper.gson.fromJson(response.body.toString(), listOf<Rental>().javaClass)
        }

        return ResponseEntity.ok(rentals)
    }

    @GetMapping("/rental/{rentalUid}")
    fun getUsersRental(
        @PathVariable("rentalUid") rentalUid: UUID,
        @RequestHeader("User-Name") username: String
    ): ResponseEntity<Rental> {
        val request =
            OkHttpKeeper
                .builder
                .url(OkHttpKeeper.RENTAL_URL + "/$rentalUid")
                .addHeader("User-Name", username)
                .get()
                .build()

        val rental = ClientKeeper.client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) null
            else GsonKeeper.gson.fromJson(response.body.toString(), Rental::class.java)
        }

        return if (rental != null && rental.mUsername == username) ResponseEntity.ok(rental)
            else ResponseEntity.badRequest().build()
    }

    @PostMapping("/rental")
    fun reserveRental(
        @RequestHeader("User-Name") username: String,
        @RequestBody reservation: RentalReservation
    ) : ResponseStatus {
        val carRequest =
            OkHttpKeeper
                .builder
                .url(OkHttpKeeper.CARS_URL + "/${reservation.carUid}")
                .get()
                .build()

        val car = ClientKeeper.client.newCall(carRequest).execute().use { response ->
            if (!response.isSuccessful) null
            else GsonKeeper.gson.fromJson(response.body.toString(), Car::class.java)
        } ?: return ResponseStatus(HttpStatus.NOT_FOUND)

        if (!car.availability)
            return ResponseStatus(HttpStatus.NOT_FOUND)

        val reserveCarRequest =
            OkHttpKeeper
                .builder
                .url(OkHttpKeeper.CARS_URL + )
                .post()

    }

}