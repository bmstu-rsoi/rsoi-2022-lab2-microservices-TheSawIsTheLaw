package services.payment.controller

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import services.payment.entity.Payment
import services.payment.repository.PaymentRepository
import java.util.*

@Controller
@RequestMapping("api/v1/payment")
class PaymentController {

    @GetMapping("/{paymentUid}")
    fun getPayment(@PathVariable paymentUid: UUID): ResponseEntity<Payment> =
        ResponseEntity.ok(PaymentRepository.get(paymentUid))

    @PatchMapping("/{paymentUid}")
    fun cancelPayment(@PathVariable paymentUid: UUID): ResponseEntity<*> =
        ResponseEntity.ok(PaymentRepository.cancelPayment(paymentUid))

    @PostMapping("/")
    fun addPayment(@RequestBody payment: Payment): ResponseEntity<Int> =
        ResponseEntity.ok(PaymentRepository.add(payment))
}