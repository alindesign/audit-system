# Audit Diff tool
[Challenge](/Challenge.md)

### Requirements
The application rely on Kotlin and Gradle, make sure you have Kotlin proper 
setup on your machine. 

### Getting started
```
# Running application (default example)
$ ./gradlew run

# Running tests
$ ./gradlew test
```

For running application via IDE consider using configured run configurations 
for IntelliJ IDEA:
- `app:run` for running application
- `app:test` for running tests

### How it works
The application is just a basic console application that takes two objects 
and prints out the difference between them. 

Example:
```
Changes:
{"property":"metadata.signIn.lastUpdated","current":"2021-09-01"}
{"property":"notes.note1","current":"This is a note"}
{"property":"notes.note2","current":"This is another note"}
{"property":"person.firstName","previous":"James","current":"Jim"}
{"property":"services","removed":["Interior/Exterior Wash"],"added":["Oil Change"]}
{"property":"subscription.startDate","previous":"2022-01-01","current":"2022-02-01"}
{"property":"subscription.status","previous":"ACTIVE","current":"EXPIRED"}
{"property":"vehicles.1.displayName","previous":"My Car","current":"23 Ferrari 296 GTS"}
{"property":"vehicles.2.displayName","previous":"My Bike","current":"Honda CBR 1000RR"}
{"property":"vehicles.3.displayName","current":"Ford F-150"}
{"property":"vehicles.3.id","current":3}
```

### How diff tool works
The diff tool is checking between two objects based on basic flow:
- If both objects are the same, we just skip them
- If any of the object is a list, the `compareObjects` will call `compareLists` 
  method.
  - If both lists are the same, we just skip them
  - If lists consists of one being a primitive list and another one being a 
    list of non-primitive objects, we'll be throwing `MixedListValuesException` 
    exception
  - If lists contains primitive values, we'll be calling `comparePrimitiveLists` 
    method which will find the added and removed values that will
    be later added to the changes list as a `ListUpdate` entry.
  - If lists contains non-primitive values, we'll be calling `compareObjectList`
    of which logic will create maps of both lists and compare them by keys using 
    `compareMap`, keys of which will be constructed from the `id` field or the 
    field annotated with `@AuditKey` annotation.
- If any of the objects is a map, the `compareObjects` will call `compareMap` 
  method
  - If both maps are the same, we just skip them
  - all the unique keys of both objects will be merged and looped through to 
    find the differences between both objects based on each key by calling 
    `compareObjects` with their values.
- If both objects are primitive values, we'll be comparing them directly and
  if they are different we'll be adding them to the changes list as a 
  `PropertyUpdate` entry.

### Exceptions
- MixedListValuesException - thrown when comparing lists of different types
- MissingAuditKeyException - thrown when comparing lists of non-primitive objects
  without `id` field or `@AuditKey` annotation
- MultipleAuditKeyException - thrown when comparing lists of non-primitive 
  objects with multiple fields annotated with `@AuditKey` annotation

### How much time did it take
It took me around 4-5 hours to complete the task. I've spent most of the time
on building and testing the diff tool (2-3hours), while the rest of the time was
allocated to optimize, research for best practices and document the project.

Over the weekend I've been checking for other possible issues and added more testing to
increase code coverage.
