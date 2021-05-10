## Antaeus

Antaeus (/ænˈtiːəs/), in Greek mythology, a giant of Libya, the son of the sea god Poseidon and the Earth goddess Gaia. He compelled all strangers who were passing through the country to wrestle with him. Whenever Antaeus touched the Earth (his mother), his strength was renewed, so that even if thrown to the ground, he was invincible. Heracles, in combat with him, discovered the source of his strength and, lifting him up from Earth, crushed him to death.

Welcome to our challenge.

## The challenge

As most "Software as a Service" (SaaS) companies, Pleo needs to charge a subscription fee every month. Our database contains a few invoices for the different markets in which we operate. Your task is to build the logic that will schedule payment of those invoices on the first of the month. While this may seem simple, there is space for some decisions to be taken and you will be expected to justify them.

## Instructions

Fork this repo with your solution. Ideally, we'd like to see your progression through commits, and don't forget to update the README.md to explain your thought process.

Please let us know how long the challenge takes you. We're not looking for how speedy or lengthy you are. It's just really to give us a clearer idea of what you've produced in the time you decided to take. Feel free to go as big or as small as you want.

## Proposed solution

The solution uses a coroutine that is launched every day. It checks if it's the first day of the month, if that's the case it:
- Retrieves the pending invoices from the database
- Attempts to pay those invoices, retrying upto 3 times if a `NetworkException` is thrown
- Updates the status of the processed invoices in the database
- Notifies via email to the team that owns the process, to let them know when it runs successfully
- Also added the POST `/rest/v1/invoices/pay` endpoint to trigger the process manually, in case that's needed.

### First steps

The first thing I wanted to do, is to understand how to create a scheduler to pay the pending invoices in the database, given that is the first time I work with Kotlin.
Instinctively, I tried to understand how to create a cron job using Kotlin and Javalin, without luck.

After reading docs and posts about people trying to achieve the same thing, I decided that the way to go was to use a coroutine.
As I had no idea about how they worked or how to use them, I decided to start building the logic we will trigger from our coroutine.

### Building the solution

You can see the progression in my commits. Basically:
- Built logic that retrieves pending invoices only
- Built logic that to call the payment provider for pending invoices, with minimal error handling
- Added endpoint to trigger the process manually, in case it's needed for some reason, or the automatic scheduled task fails.
- Added retry logic in the BillingService, to retry payment when a NetworkException is thrown
- Added unit tests
- Added logic that updates the paid invoices status in the database
- Added EmailProvider and EmailService classes to notify when the process runs successfully. (It's just a mock for the purpose of the challenge, there's no real logic built)
- Created coroutine that runs everyday


## Developing

Requirements:
- \>= Java 11 environment

Open the project using your favorite text editor. If you are using IntelliJ, you can open the `build.gradle.kts` file and it is gonna setup the project in the IDE for you.

### Building

```
./gradlew build
```

### Running

There are 2 options for running Anteus. You either need libsqlite3 or docker. Docker is easier but requires some docker knowledge. We do recommend docker though.

*Running Natively*

Native java with sqlite (requires libsqlite3):

If you use homebrew on MacOS `brew install sqlite`.

```
./gradlew run
```

*Running through docker*

Install docker for your platform

```
docker build -t antaeus
docker run antaeus
```

### App Structure
The code given is structured as follows. Feel free however to modify the structure to fit your needs.
```
├── buildSrc
|  | gradle build scripts and project wide dependency declarations
|  └ src/main/kotlin/utils.kt 
|      Dependencies
|
├── pleo-antaeus-app
|       main() & initialization
|
├── pleo-antaeus-core
|       This is probably where you will introduce most of your new code.
|       Pay attention to the PaymentProvider and BillingService class.
|
├── pleo-antaeus-data
|       Module interfacing with the database. Contains the database 
|       models, mappings and access layer.
|
├── pleo-antaeus-models
|       Definition of the Internal and API models used throughout the
|       application.
|
└── pleo-antaeus-rest
        Entry point for HTTP REST API. This is where the routes are defined.
```

### Main Libraries and dependencies
* [Exposed](https://github.com/JetBrains/Exposed) - DSL for type-safe SQL
* [Javalin](https://javalin.io/) - Simple web framework (for REST)
* [kotlin-logging](https://github.com/MicroUtils/kotlin-logging) - Simple logging framework for Kotlin
* [JUnit 5](https://junit.org/junit5/) - Testing framework
* [Mockk](https://mockk.io/) - Mocking library
* [Sqlite3](https://sqlite.org/index.html) - Database storage engine

Happy hacking 😁!
