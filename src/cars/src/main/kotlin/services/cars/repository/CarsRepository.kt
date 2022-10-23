package services.cars.repository

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository
import services.cars.data.CarsTable
import services.cars.data.CarsTable.availability
import services.cars.data.CarsTable.brand
import services.cars.data.CarsTable.carUid
import services.cars.data.CarsTable.model
import services.cars.data.CarsTable.power
import services.cars.data.CarsTable.price
import services.cars.data.CarsTable.registrationNumber
import services.cars.data.CarsTable.type
import services.cars.entity.Car
import services.cars.insecure.Config

object CarsRepository {

    private val db by lazy {
        Database.connect(
            Config.POSTGRES_DB_ADDRESS,
            "org.postgresql.Driver",
            Config.POSTGRES_USER,
            Config.POSTGRES_PASSWORD
        )
    }

    fun get(showAll: Boolean = false): Array<Car> =
        transaction(db) {
            val preset = if (showAll) CarsTable.selectAll() else CarsTable.select(availability eq true)

            preset
                .map { car ->
                    Car(
                        car[CarsTable.id],
                        car[carUid],
                        car[brand],
                        car[model],
                        car[registrationNumber],
                        car[price],
                        car[availability],
                        car[power],
                        car[type]
                    )
                }
                .toTypedArray()
        }

    fun get(id: Int): Car? =
        transaction(db) {
            CarsTable
                .select(CarsTable.id eq id)
                .map { car ->
                    Car(
                    car[CarsTable.id],
                    car[carUid],
                    car[brand],
                    car[model],
                        car[registrationNumber],
                        car[price],
                        car[availability],
                        car[power],
                        car[type]
                ) }
                .toTypedArray()
                .firstOrNull()
        }
}