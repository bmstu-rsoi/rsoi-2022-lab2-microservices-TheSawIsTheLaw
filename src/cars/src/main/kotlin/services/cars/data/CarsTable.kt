package services.cars.data

import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.Table

object CarsTable: Table() {

    val id = integer("id").autoIncrement().isNotNull()
    val carUid = uuid("car_uid").isNotNull()
    val brand = varchar("brand", 80).isNotNull()
    val model = varchar("model", 80).isNotNull()
    val registrationNumber = varchar("registration_number", 20).isNotNull()
    val power = integer("power")
    val price = integer("price").isNotNull()
    val type = varchar("type", 20).check { it.inList(listOf("SEDAN", "SUV", "MINIVAN", "ROADSTER")) }
    val availability = bool("availability").isNotNull()
}