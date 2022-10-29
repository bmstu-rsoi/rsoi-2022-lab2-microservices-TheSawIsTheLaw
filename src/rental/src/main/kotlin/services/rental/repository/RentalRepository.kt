package services.rental.repository

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import services.rental.data.RentalTable
import services.rental.data.RentalTable.carUid
import services.rental.data.RentalTable.dateFrom
import services.rental.data.RentalTable.dateTo
import services.rental.data.RentalTable.paymentUid
import services.rental.data.RentalTable.rentalUid
import services.rental.data.RentalTable.status
import services.rental.entity.Rental
import services.rental.insecure.Config
import java.util.UUID

object RentalRepository {

    private val db by lazy {
        Database.connect(
            Config.POSTGRES_DB_ADDRESS,
            "org.postgresql.Driver",
            Config.POSTGRES_USER,
            Config.POSTGRES_PASSWORD
        )
    }

    fun get(username: String): Array<Rental> =
        transaction(db) {
            RentalTable
                .select(RentalTable.username eq username)
                .map { rental ->
                    Rental(
                        rental[RentalTable.id],
                        rental[rentalUid],
                        rental[RentalTable.username],
                        rental[paymentUid],
                        rental[carUid],
                        rental[dateFrom],
                        rental[dateTo],
                        rental[status]
                    )
                }
                .toTypedArray()
        }

    fun get(rentalUid: UUID): Rental? =
        transaction(db) {
            RentalTable
                .select(RentalTable.rentalUid eq rentalUid)
                .map { rental ->
                    Rental(
                        rental[RentalTable.id],
                        rental[RentalTable.rentalUid],
                        rental[RentalTable.username],
                        rental[paymentUid],
                        rental[carUid],
                        rental[dateFrom],
                        rental[dateTo],
                        rental[status]
                    )
                }
                .firstOrNull()
        }

    fun add(rental: Rental) =
        transaction(db) {
            RentalTable
                .insert {
                    it[rentalUid] = rental.mRentalUid
                    it[username] = rental.mUsername
                    it[paymentUid] = rental.mPaymentUid
                    it[carUid] = rental.mCarUid
                    it[dateFrom] = rental.mDateFrom
                    it[dateTo] = rental.mDateTo
                    it[status] = rental.mStatus
                }.resultedValues!!.first()[RentalTable.id]
        }

    fun setStatus(uid: UUID, newStatus: String) =
        transaction(db) {
            RentalTable
                .update({ rentalUid eq uid }) {
                    it[status] = newStatus
                }
        }
}