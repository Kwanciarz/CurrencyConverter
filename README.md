# Currency Converter App for Android
## General Info
This app allows users to fetch data from the Frankfurter currency exchange API and store it locally for offline use. I encountered the need for such an app, so I made it to learn Kotlin and Android development.

## Availability
The app is currently not available for download. I plan to publish it for free once it has sufficient functionality and thorough testing.

## Features
- Offline
  - Check exchange values for selected currencies
  - Select currencies from those already added
  - Remove currencies from the list
- Online
  - Add new currencies available in the Frankfurter API
  - Search for currencies to add by code and name
## Showcase
Converting currency values yields results quickly.

![AnimationOne](https://github.com/Kwanciarz/CurrencyConverter/assets/42122846/3178d8d2-eb42-4141-b5ef-98fded4d7a22)

The screen showing available currencies clearly displays the selected options.

![AnimationTwo](https://github.com/Kwanciarz/CurrencyConverter/assets/42122846/db834ae9-14c0-4136-9680-348ea2ede570)

## Technology
- Kotlin
- Jetpack Compose
- Coroutines
- Ktor

## API Integration
To reduce data transfer, only new exchange rates are fetched when a user adds a new currency. Future plans include adding a condition to refresh the entire exchange rate table only once a day, aligning with the Frankfurter database update schedule.

## Conclusion
This is personal project aimed at enhancing my coding skills and knowledge. It's not intended as a finished product but rather a learning tool. Your feedback is valuable for my growth, but please note that changes and improvements are ongoing as I continue to learn.
