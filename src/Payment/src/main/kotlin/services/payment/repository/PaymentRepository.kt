package services.payment.repository

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import services.payment.data.PaymentTable
import services.payment.data.PaymentTable.price
import services.payment.data.PaymentTable.status
import services.payment.entity.Payment
import services.payment.insecure.Config
import java.util.*

object PaymentRepository {

    private val db by lazy {
        Database.connect(
            Config.POSTGRES_DB_ADDRESS,
            "org.postgresql.Driver",
            Config.POSTGRES_USER,
            Config.POSTGRES_PASSWORD
        )
    }

    fun get(paymentUid: UUID) =
        transaction(db) {
            PaymentTable
                .select(PaymentTable.paymentUid eq paymentUid)
                .map { payment ->
                    Payment(
                        payment[PaymentTable.id],
                        paymentUid,
                        payment[status],
                        payment[price]
                    )
                }
                .firstOrNull()
        }

    fun add(payment: Payment) =
        transaction(db) {
            PaymentTable.insert {
                it[paymentUid] = payment.mPaymentUid
                it[status] = payment.mStatus
                it[price] = payment.mPrice
            }.resultedValues!!.first()[PaymentTable.id]
        }

    fun cancelPayment(paymentUid: UUID) =
        transaction(db) {
            PaymentTable.update({ PaymentTable.paymentUid eq paymentUid }) {
                it[status] = "CANCELED"
            }
        }
}