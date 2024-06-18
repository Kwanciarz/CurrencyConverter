# Currency Converter app for Android
## General info
This app allows user to fetch data from Frankfurter currency exchange API and store it locally, to allow for offline use.
I've encoutererd the need of such app so I made it and used it to learn Kotlin and Android development.
## This app is not avaiable yet to download
I might publish the app for free once it has enough functionality and is thoroughly tested.

## Features
- Offline
  - Checking exchange values for selected currencies
  - Selecting currencies from already added ones
  - Removing currencies from list
- Online
  - Adding new currencies avaiable in Frankfurer Api
  - Searching currencies to add by code and name


## Technology
- Corutines
- Ktor
- Jetpack Compose
- Kotlin

## API integration
To minimize data transfer, only new exchange rates are fetched when a user adds a new currency.
In the future, I plan to add a condition to refresh the entire exchange rate table only once a day (when the Frankfurter database is also refreshed).

## Feedback Welcome!
This is a work in progress, and I'm a begginer.
