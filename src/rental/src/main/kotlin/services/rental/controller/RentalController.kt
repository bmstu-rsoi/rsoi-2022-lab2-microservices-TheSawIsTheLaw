package services.rental.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import services.rental.entity.Rental
import services.rental.repository.RentalRepository
import java.util.*

@Controller
@RequestMapping("/api/v1/rental")
class RentalController {

    @GetMapping("/")
    fun getRentals(@RequestHeader("User-Name") username: String): ResponseEntity<Array<Rental>> =
        ResponseEntity.ok(RentalRepository.get(username))

    @GetMapping("/{rentalUid}")
    fun getRental(@PathVariable rentalUid: UUID): ResponseEntity<Rental> =
        ResponseEntity.ok(RentalRepository.get(rentalUid))

    @PostMapping("/")
    fun addRental(@RequestBody rental: Rental): ResponseEntity<Int> =
        ResponseEntity.ok(RentalRepository.add(rental))

    @PatchMapping("/{rentalUid}/finish")
    fun finishRental(@PathVariable rentalUid: UUID): ResponseStatus =
        ResponseStatus(HttpStatus.OK).apply { RentalRepository.setStatus(rentalUid, "FINISHED") }

    @PatchMapping("/{rentalUid}/cancel")
    fun cancelRental(@PathVariable rentalUid: UUID): ResponseStatus =
        ResponseStatus(HttpStatus.OK).apply { RentalRepository.setStatus(rentalUid, "CANCELED") }
}