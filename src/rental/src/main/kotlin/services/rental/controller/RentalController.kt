package services.rental.controller

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import services.rental.entity.Rental
import services.rental.repository.RentalRepository
import java.util.*

@Controller
@RequestMapping("/api/v1/rental")
class RentalController {

    @GetMapping("/")
    fun getRentals(@RequestHeader username: String): ResponseEntity<Array<Rental>> =
        ResponseEntity.ok(RentalRepository.get(username))

    @GetMapping("/{rentalUid}")
    fun getRental(@PathVariable rentalUid: UUID): ResponseEntity<Rental> =
        ResponseEntity.ok(RentalRepository.get(rentalUid))
}