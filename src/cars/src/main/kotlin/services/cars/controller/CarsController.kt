package services.cars.controller

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import services.cars.entity.Car
import services.cars.repository.CarsRepository

@Controller
@RequestMapping("/api/v1/cars")
class CarsController(
) {

    @GetMapping("/")
    fun getCars(@RequestParam("showAll") showAll: Boolean): ResponseEntity<Array<Car>> =
        ResponseEntity.ok(CarsRepository.get(showAll))

    @GetMapping("/{id}")
    fun getCar(@PathVariable id: Int): ResponseEntity<Car> =
        ResponseEntity.ok(CarsRepository.get(id))
}