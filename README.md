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
- Hiding the app by changing the app-name and -icon to "Pocket Calculator" with a calculator-icon.

## What the app will be able to do in the future

- Support of additional features of a contact app
  - Mark contacts as favorites
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

## User Guide
### Settings
This sections explains the meaning and workings of the settings-screen.

#### Section "Display"
- _App theme_: whether the app is displayed in light-theme, dark-theme or the system-default.
- _Order by first name_: whether the contact-list should be ordered by first- or last-name.
- _Show contact type in contact list_: whether the contact-type (public or secret) is depicted in the list of contacts using a lock-icon. The lock is closed and green for secret contacts. It is open and red for public contacts.
- _Show additional save-button in the edit-screen_: this setting is about the screen which can edit contacts or create a new one. The primary save-button is in the navigation-bar, in the top-right of the screen. If this checkbox is set, another one will be added at the bottom for reasons of convenience.
- _Show the navigation-bar at the bottom of the screen_: inverts the position of the navigation-bar, showing it at the bottom instead of the default top. This can be more convenient on large-screen phones.
- _Show WhatsApp buttons_: if enabled, a buttons will be shown next to all phone-numbers on the detail-screen (with the overview over a contact). These buttons show the icon of WhatsApp and - when pressed - will open WhatsApp in a chat with this contact.
  - This allows the user to start a new chat in WhatsApp with that contact, sharing only that single phone-number (no name no other information)
  - As we cannot check if that phone-number is actually connected to a WhatsApp account without giving the number to WhatsApp (which is the last thing we want), this button is shown next to **all** phone-numbers independent of whether they are registered on WhatsApp or not.

#### Section "Call detection"
- _Match incoming calls with contacts_: whether the app should try to detect when a secret contact is calling them. See the technical section below for details.
- _Show information on lock-screen_: whether the notification informing about a caller in the list of secret-contacts should be shown before the phone is unlocked.
- _Block calls from unknown numbers_: whether the app should block all incoming calls from numbers which are not stored in your contacts (either secret or public). This feature only works with Android 10 or newer.

#### Section "Contact list"
- _Show public contacts_: whether the app should just show you your secret contacts or also the public ones which are managed by your phone's standard contacts-app and the operating system. If this checkbox is set, the app will need the permission to access your phone's contacts. It will then populate a second tab showing both secret and public contacts.
- _Show third-party contact accounts_: if disabled, the app only allows you to store public contacts in either your Google-account or your phone's local contacts. If enabled, any account stored on the device will be allowed.
  - Beware: You may need to restart the app after changing this setting.
  - BEWARE: We cannot reliably determine whether any selected account is actually able to store your contacts. If you choose an account unsuited for storing contacts, this might lead to the loss of your data. Be careful.
- _Second tab_: the first tab will always show the list of _secret_ contacts. This setting allows the user to define what the second tab should show: either _all_ contacts or just the _public_ ones.

#### Section "Default values"
All settings in this section define defaults which can be overridden by the user on the spot.
- _Contact-type_: whether a newly created contact should be _public_ or _secret_ by default (can ofc be changed during creation).
- _Contact account_: only relevant for public contacts, defines where they should be stored (e.g. phone-local or in your google account).
- _VCF version_: VCF is the format in which contacts are exported and can be imported in other contacts apps. This format has a newer version 4 and an older version 3. The older version has fewer features but is more likely to be compatible with older contacts-apps.

#### Security
- _App authentication_: will add an authentication-step in the app startup. This means that the user will e.g. have to authenticate by fingerprint or face-id before seing the list of contacts. Disclaimer: the app does not implement any of the authentication-methods itself but uses the standard-authentication defined by the operating system (Google is better at this kind of thing).

#### Privacy
- _Hide app name and icon_: changes the name and icon of the app on the home-screen and in the app-overview. The app pretends to be a simple calculator app named "Pocket Calculator" (the name was chosen to start with the same letter so the app can be more easily found in alphabetical sorting).
  - However, in the system-settings the true name will still appear.
  - Dependening on your phone, operating system and launcher, you will have to restart your phone to see the change - we appologize for the inconvenience but that is outside of our control.
- _Send anonymous error reports_: if the app crashes or something goes wrong during its operation, we won't notice unless it happens on one of our phones. That is not satisfying because many bugs only appear under very specific circumstances (like only on one specific model or manufacturer). Therefore, we use Google's "Crashlytics" framework to get error-reports. We make sure that no sensitive information is written into these reports. However, you can ofc turn this off if it makes you uncomfortable. In that case, please let us know about bugs by email.

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
