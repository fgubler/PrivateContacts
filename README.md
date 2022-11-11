# Private Contacts #

_Private Contacts_ is an Android app to manage your contacts with additional features for **privacy** and **data protection** not offered by your phone's default contacts app.

When any app (e.g. a messenger like WhatsApp) asks for permission to access your phone's contacts, the choice is purely binary: You either give it access to **all** your contacts or none of them. In that moment you will likely feel fine about sharing the phone-numbers of your friends who also have the same app installed, anyway. However, what about the phone number of your doctor, your therapist, etc.? Do you really want to provide that information to everyone asking? Often, the mere presence of that contact on your phone is enough to deduce a lot about your health, preferences and behavior.

## What the app can do right now
Right now, the app simply offers you the typical functionalities of a contacts app without sharing its contacts with any other app. If an app has the permission to access the phone's contacts, it will **not** get those stored in _Private Contacts_ anyway.

The app supports caller-detection, displaying a notification if a known contact is calling you. Unfortunately, that depends heavily on manufacturer and Android version. For that reason it is not 100% reliable yet (feedback is very welcome).

Additional features
- Displaying the normal contacts of the phone.
- Moving contacts from the standard Android database to the app (and removing them from the standard database so they are no longer visible to other apps)

## What the app will be able to do in the future
- Connecting the app to the standard Android database for contacts. 
  - Editing existing contacts from the standard Android database
  - Creating new contacts in the standard Android database
  - Moving contacts from Private Contacts back the the standard Android contact database
- Support of Imports/Exports in vcf format
- Support of additional features of a contact app
  - Profile images
  - Mark contacts as favorites
  - Add contacts to groups/labels
- Improvements of caller detection
- Maybe an additional category of "Anonymized" contacts which are shared with other apps but under an alias.
- Encryption and password protection
- Suggestions are always welcome...

## Screenshots

|Start Screen|Contact Details|Contact Edit 1|Contact Edit 2|
|------------|---------------|--------------|---------------|
|![Contact List](https://user-images.githubusercontent.com/1478872/164909358-a0277229-c2c7-42bb-99a1-c5425b400946.jpg)|![Contact Details](https://user-images.githubusercontent.com/1478872/164909363-3ecbbcb0-5cb0-4284-943f-d5e4d42c71c3.jpg)|![Contact Edit 1](https://user-images.githubusercontent.com/1478872/164909371-c0cdbe58-ce72-4333-971b-125b1e64747c.jpg)|![Contact Edit 2](https://user-images.githubusercontent.com/1478872/164909374-ab98a2d4-945d-4775-a0a8-2c04054e7e95.jpg)|

## Used Libraries
(this is a non-complete list of the most important libraries used; please consult the build.gradle for the complete list)
- "Jetpack Compose" by Google for the UI: https://developer.android.com/jetpack/compose
- "Contact Store" by Alex Styl for accessing the contacts: https://github.com/alexstyl/contactstore
- "libaddressinput" by Google for formatting addresses: https://github.com/google/libaddressinput

## Contact
* Florian Gubler
* 2Gusoft@gmail.com
* App entry in Google Play Store: [PrivateContacts](https://play.google.com/store/apps/details?id=ch.abwesend.privatecontacts)
