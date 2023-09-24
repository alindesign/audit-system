import diff.*
import models.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


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

        assertEquals(
            changes, listOf(
                PropertyUpdate("notes.note2", "This is another note", "This is another note but two"),
                PropertyUpdate("person.firstName", "James", "Jim"),
                ListUpdate("services", listOf("Interior/Exterior Wash"), listOf("Oil Change")),
                PropertyUpdate("subscription.startDate", "2022-01-01", "2022-02-01"),
                PropertyUpdate("subscription.status", "ACTIVE", "EXPIRED"),
                PropertyUpdate("vehicles.1.displayName", "My Car", "23 Ferrari 296 GTS"),
                PropertyUpdate("vehicles.2.displayName", "My Bike", "Honda CBR 1000RR"),
                PropertyUpdate("vehicles.3.displayName", null, "Ford F-150"),
                PropertyUpdate("vehicles.3.id", null, 3)
            )
        )
    }

    @Test
    fun `test comparing lists with invalid audit key`() {
        data class InvalidAuditKey(val name: String)

        val list1 = listOf(InvalidAuditKey("name1"), InvalidAuditKey("name2"))
        val list2 = listOf(InvalidAuditKey("name1"), InvalidAuditKey("name2"), InvalidAuditKey("name3"))

        val diffTool = DiffTool()
        assertFailsWith<InvalidAuditKeyException> {
            diffTool.diff(list1, list2)
        }
    }

    @Test
    fun `test comparing lists with multiple audit key`() {
        data class MultipleAuditKey(
            @AuditKey val key1: String,
            @AuditKey val key2: String,
            val name: String
        )

        val list1 = listOf(
            MultipleAuditKey("key1.name1", "key2.name1", "name1"),
            MultipleAuditKey("key1.name2", "key2.name2", "name2")
        )
        val list2 = listOf(
            MultipleAuditKey("key1.name1", "key2.name1", "name1"),
            MultipleAuditKey("key1.name2", "key2.name2", "name2"),
            MultipleAuditKey("key1.name3", "key2.name3", "name3")
        )

        val diffTool = DiffTool()
        assertFailsWith<MultipleAuditKeyException> {
            diffTool.diff(list1, list2)
        }
    }

    @Test
    fun `test comparing lists with nullable values`() {
        data class Item(
            val id: String,
            val name: String
        )

        val list1 = listOf(
            Item("name1.id", "name1"),
            Item("name2.id", "name2")
        )
        val list2 = listOf(
            Item("name1.id", "name1"),
            Item("name2.id", "name2"),
            null
        )

        val diffTool = DiffTool()
        assertFailsWith<InvalidAuditKeyException> {
            diffTool.diff(list1, list2)
        }
    }

    @Test
    fun `test AuditKey list values`() {
        data class Item(
            @AuditKey val key: String,
            val name: String
        )

        val list1 = listOf(
            Item("name1_id", "name1"),
            Item("name2_id", "name2")
        )
        val list2 = listOf(
            Item("name1_id", "name1"),
            Item("name2_id", "name2"),
            Item("name3_id", "name3")
        )

        val diffTool = DiffTool()
        val changes = diffTool.diff(list1, list2)

        assertEquals(2, changes.size)
        assertEquals(2, changes.filterIsInstance<PropertyUpdate<*>>().size)
        assertEquals(
            changes, listOf(
                PropertyUpdate("name3_id.key", null, "name3_id"),
                PropertyUpdate("name3_id.name", null, "name3"),
            )
        )
    }

    @Test
    fun `test mixed list values`() {
        data class Item(
            val id: String
        )

        val list1 = listOf(
            "name1",
            "name2"
        )

        val list2 = listOf(
            "name1",
            "name2",
            Item("name3")
        )

        val diffTool = DiffTool()
        assertFailsWith<MixedListValuesException> {
            diffTool.diff(list1, list2)
        }
    }
}
