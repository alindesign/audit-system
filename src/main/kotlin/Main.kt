import com.google.gson.Gson
import diff.DiffTool
import diff.ListUpdate
import diff.PropertyUpdate
import models.*

fun main() {
    val previousAccount = Account(
        1,
        Person(1, "James", "Smith"),
        Subscription("ACTIVE", "2022-01-01"),
        listOf(Vehicle(1, "My Car"), Vehicle(2, "My Bike")),
        listOf("General", "Interior/Exterior Wash")
    )

    val currentAccount = Account(
        1,
        Person(1, "Jim", "Smith"),
        Subscription("EXPIRED", "2022-02-01"),
        listOf(Vehicle(1, "23 Ferrari 296 GTS"), Vehicle(2, "Honda CBR 1000RR"), Vehicle(3, "Ford F-150")),
        listOf("General", "Oil Change"),
        mapOf("signIn" to Metadata("2021-09-01")),
        mapOf("note1" to "This is a note", "note2" to "This is another note")
    )

    // Using the DiffTool to find the differences
    val diffTool = DiffTool()
    val changes = diffTool.diff(previousAccount, currentAccount)

    // Display the changes
    println("Changes:")
    changes.forEach { item ->
        Gson().toJson(item).also { println(it) }
    }
}
