import diff.DiffTool
import diff.ListUpdate
import diff.PropertyUpdate
import models.Account
import models.Person
import models.Subscription
import models.Vehicle
import models.Metadata
import kotlin.test.Test
import kotlin.test.assertEquals


class DiffToolTests {
    @Test
    fun `test comparing two identical accounts`() {
        val account1 = Account(
            1,
            Person(1, "James", "Smith"),
            Subscription("ACTIVE", "2022-01-01"),
            listOf(Vehicle(1, "My Car"), Vehicle(2, "My Bike")),
            listOf("General", "Interior/Exterior Wash"),
            mapOf("signIn" to Metadata("2021-09-01")),
            mapOf("note1" to "This is a note", "note2" to "This is another note")
        )

        val account2 = Account(
            1,
            Person(1, "James", "Smith"),
            Subscription("ACTIVE", "2022-01-01"),
            listOf(Vehicle(1, "My Car"), Vehicle(2, "My Bike")),
            listOf("General", "Interior/Exterior Wash"),
            mapOf("signIn" to Metadata("2021-09-01")),
            mapOf("note1" to "This is a note", "note2" to "This is another note")
        )

        val diffTool = DiffTool()
        val changes = diffTool.diff(account1, account2)

        assertEquals(0, changes.size)
    }

    @Test
    fun `test comparing two different accounts`() {
        val account1 = Account(
            1,
            Person(1, "James", "Smith"),
            Subscription("ACTIVE", "2022-01-01"),
            listOf(Vehicle(1, "My Car"), Vehicle(2, "My Bike")),
            listOf("General", "Interior/Exterior Wash"),
            mapOf("signIn" to Metadata("2021-09-01")),
            mapOf("note1" to "This is a note", "note2" to "This is another note")
        )

        val account2 = Account(
            1,
            Person(1, "Jim", "Smith"),
            Subscription("EXPIRED", "2022-02-01"),
            listOf(Vehicle(1, "23 Ferrari 296 GTS"), Vehicle(2, "Honda CBR 1000RR"), Vehicle(3, "Ford F-150")),
            listOf("General", "Oil Change"),
            mapOf("signIn" to Metadata("2021-09-01")),
            mapOf("note1" to "This is a note", "note2" to "This is another note but two")
        )

        val diffTool = DiffTool()
        val changes = diffTool.diff(account1, account2)

        assertEquals(9, changes.size)
        assertEquals(8, changes.filterIsInstance<PropertyUpdate<*>>().size)
        assertEquals(1, changes.filterIsInstance<ListUpdate<*>>().size)

        assertEquals(changes, listOf(
            PropertyUpdate("notes.note2", "This is another note", "This is another note but two"),
            PropertyUpdate("person.firstName", "James", "Jim"),
            ListUpdate("services", listOf("Interior/Exterior Wash"), listOf("Oil Change")),
            PropertyUpdate("subscription.startDate", "2022-01-01", "2022-02-01"),
            PropertyUpdate("subscription.status", "ACTIVE", "EXPIRED"),
            PropertyUpdate("vehicles.1.displayName", "My Car", "23 Ferrari 296 GTS"),
            PropertyUpdate("vehicles.2.displayName", "My Bike", "Honda CBR 1000RR"),
            PropertyUpdate("vehicles.3.displayName", null, "Ford F-150"),
            PropertyUpdate("vehicles.3.id", null, 3)
        ))
    }
}
