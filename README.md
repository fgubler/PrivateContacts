# Private Contacts #

_Private Contacts_ is an Android app to manage your contacts with additional features for **privacy** and **data protection** not offered by your phone's default contacts app.

When any app (e.g. a messenger like WhatsApp) asks for permission to access your phone's contacts, the choice is purely binary: You either give it access to **all** your contacts or none of them. In that moment you will likely feel fine about sharing the phone-numbers of your friends who also have the same app installed, anyway. However, what about the phone number of your doctor, your therapist, etc.? Do you really want to provide that information to everyone asking? Often, the mere presence of that contact on your phone is enough to deduce a lot about your health, preferences and behavior.

## What the app can do right now
Right now, the app simply offers you the typical functionalities of a contacts app without sharing its contacts with any other app. If an app has the permission to access the phone's contacts, it will **not** get those stored in _Private Contacts_ anyway.

The app supports caller-detection, displaying a notification if a known contact is calling you. Unfortunately, that depends heavily on manufacturer and Android version. For that reason it is not 100% reliable yet (feedback is very welcome).

Additional features
- Displaying & editing the normal contacts of the phone.
- Moving contacts from the standard Android database to the app (and removing them from the standard database so they are no longer visible to other apps)
- Creating new contacts in the standard Android database
- Moving contacts from Private Contacts back the the standard Android contact database (in case that is desired)
- Support of Imports/Exports in vcf format
- Protecting the app with biometric prompt

## What the app will be able to do in the future

- Support of additional features of a contact app
  - Profile images
  - Mark contacts as favorites
  - Add contacts to groups/labels
- Improvements of caller detection
- Maybe an additional category of "Anonymized" contacts which are shared with other apps but under an alias.
- Encryption and password protection
- Suggestions are always welcome...

## Technical Restrictions
- What we would like to do is provide contacts to e.g. the official phone-app while withholding them from e.g. WhatsApp. Unfortunately, Android does not allow this: we cannot provide a contact to some apps but not to others.
  - Either a contact is in the public contact-database where every app with the necessary permission can read it;
  - or it is secret and no app (other) can read it.
- Both Google and phone manufacturers like Samsung tightly restrict call-detection (i.e. reacting to an incoming call by e.g. showing a popup). This is good because it improves our privacy. Unfortunately, it also restricts what we can do within this app. We try to detect incoming calls and show notifications but the corresponding logic changes with every version of Android and may also vary between phone manufacturers. Therefore, the detection-feature can be a bit flaky and may not work reliably on your device. We are sorry for the inconvenience and happy about feedback (e.g. on which devices it does not work or also technical suggestions for improving it).   

## Screenshots

|Start Screen|Contact Details|Contact Edit 1|Contact Edit 2|
|------------|---------------|--------------|---------------|
|![Contact List](https://user-images.githubusercontent.com/1478872/164909358-a0277229-c2c7-42bb-99a1-c5425b400946.jpg)|![Contact Details](https://user-images.githubusercontent.com/1478872/164909363-3ecbbcb0-5cb0-4284-943f-d5e4d42c71c3.jpg)|![Contact Edit 1](https://user-images.githubusercontent.com/1478872/164909371-c0cdbe58-ce72-4333-971b-125b1e64747c.jpg)|![Contact Edit 2](https://user-images.githubusercontent.com/1478872/164909374-ab98a2d4-945d-4775-a0a8-2c04054e7e95.jpg)|

## Technical Background
This chapter illustrates some technical background information to help users understand what happens behind the scenes.

### The Android Contact System
The "normal" contact system of Android works (a bit simplified) like this: 
- The operating system manages one big database of contacts.
- Apps with the appropriate read-permission can read contacts from this database.
- Apps with the appropriate write-permission can write to this database.

As a consequence, any contact written to this central database can be read by any other app with the contact-read permission, e.g. standard telephone- and contacts-apps, 3rd-party apps like WhatsApp, Signal, Gmail, Outlook, etc.

### What PrivateContacts does
#### Contact Storage
Once a contact is written into the central database, we lose control over it. 
- For that reason, contacts marked _secret_ are not written there.
- Contacts marked as _public_, on the other hand, are written into that database to allow other applications to find them.

PrivateContacts maintains its own, separate database to store those contacts that are marked as _secret_. That database is in the app's private directory where it cannot be accessed by other apps (unless the device is rooted (jailbroken), but in that case all security-bets are off, anyway).

#### Backups
A downside of this is ofc that we are now responsible for backing them up: they are no longer synchronized nicely over Google.
However, Google's "Google One-Backup" (which backs up the app-state of all installed apps for recovery after a factory-reset or on new phone) will cover the private directory of all apps, including the database of secret contacts. 

On first glance, this may look counter-intuitive to the promise of keeping these contacts secret, however we have decided that it is a risk worth taking, for the following reasons.
- The convenience of this automatic backup-solution is unquestionable.
- Google has promised not only publicly but under oath in the US legal system that these backups are end-to-end encrypted and cannot be read by anyone but the user.
  - We have the highest possible trust in Googles technical competence to make sure no one **else** is able to read these backups - if anything we would question their motivation to build in a back-door for themselves.
- Any user who distrusts this statement can and should disable the entire "Google One-Backup" feature anyway.
- If that is still not enough for you, probably you should not use an operating system developped and maintained by Google: in the end no app can protect you from the operating system in which it is running.

#### Call detection
The second big disadvantage of keeping our contacts out of the central contacts-database is ofc that not even the standard phone-app can get to them: we are still exploring technical possibilities to allow sharing secret contacts with select apps but have so far been unsuccessful. As a consequence, the call-screen will show an unknown number of one of your secret contacts calls you.

The experimental call-detection feature registers the app to be informed by the operating-system whenever a call is incoming. It can then match the calling number against the list of secret contacts to check if the caller is among them. If yes, the app will show a notification (at the top of the screen) with the callers name, as well as a toast at the bottom. Unfortunately, the correct working of this is strongly dependent on the phone-manufacturer "playing nice": some phone-apps will just put themselves in the foreground so strongly that neither the toast nor the notification will be visible to the user. Having "extra powers" (i.e. higher permissions) granted by the manufacturer, they rightfully "win" against any 3rd-pary app like PrivateContacts.

## Used Libraries
(this is a non-complete list of the most important libraries used; please consult the build.gradle for the complete list)
- "Jetpack Compose" by Google for the UI: https://developer.android.com/jetpack/compose
- "Contact Store" by Alex Styl for accessing the contacts: https://github.com/alexstyl/contactstore
- "libaddressinput" by Google for formatting addresses: https://github.com/google/libaddressinput

## Contact
* Florian Gubler
* 2Gusoft@gmail.com
* App entry in Google Play Store: [PrivateContacts](https://play.google.com/store/apps/details?id=ch.abwesend.privatecontacts)
