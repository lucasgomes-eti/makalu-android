# Play Console: foreground service declaration

Draft for **Policy > App content > Foreground service permissions**. The app declares
one foreground service, `OrderTrackingService`, of type **Special use**
(`FOREGROUND_SERVICE_SPECIAL_USE`). Review the text, record the video, and submit.

## What is in the manifest

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />

<service
    android:name=".tracking.OrderTrackingService"
    android:exported="false"
    android:foregroundServiceType="specialUse">
    <property
        android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
        android:value="Shows the live status of a food order the customer has just placed, in an ongoing notification, from the moment it is placed until it is delivered or cancelled." />
</service>
```

The service passes `ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE` to
`startForeground()`. It is declared in `features/orders/src/main/AndroidManifest.xml`;
the description is the string `tracking_special_use_description`.

## Text for the declaration

**Which foreground service type does your app use?**

Special use.

**Describe the task the foreground service performs.**

> Makalu is a food delivery app. When a customer places an order, the app shows an
> ongoing notification with the store's name and the order's current status (waiting
> for the store, being prepared, on its way), and plays an alert each time the status
> changes. The foreground service keeps a single network connection open to our own
> servers so these status changes reach the phone within seconds, for as long as the
> order is in progress. It stops by itself as soon as the order is delivered or
> cancelled, and the ongoing notification is replaced by a normal one the user can
> dismiss.

**Why is it user-initiated and visible to the user?**

> The service only ever starts as a direct result of the customer placing an order in
> the app (tapping "Make order"), or of the customer opening the app while one of
> their orders is still in progress. It never starts in the background, at boot, or
> on a schedule. For its whole lifetime it shows an ongoing notification for each
> order in progress, with the store name, the order number, the current status and a
> progress indicator; tapping it opens that order in the app. The service ends when
> the last order reaches a final status, or 12 hours after an order last changed.

**Why does the task need to start immediately and why can it not be paused or deferred?**

> A food order moves through its statuses in minutes. The customer needs to know at
> once that the store accepted the order and, above all, that the courier is on the
> way, so they can be ready to receive it. A notification that arrives late, or only
> when the app is next opened, has no value: the food is already at the door or the
> order was already cancelled. Deferrable work (WorkManager, JobScheduler) is
> delivered at the system's discretion, minutes to hours later, and cannot hold the
> connection the updates arrive on.

**Why do the other foreground service types not fit?**

> - *Data sync* is meant for bounded transfers and is limited to 6 hours in any 24 on
>   Android 15 and later. A customer who orders lunch and dinner on the same day would
>   exhaust it, after which the service could not start and the second order would not
>   be tracked.
> - *Remote messaging* is for transferring text messages between a user's devices,
>   which this is not.
> - *Short service* lasts about 3 minutes; an order takes far longer.
> - *Location*, *connected device*, *media playback*, *media processing*, *phone call*,
>   *camera*, *microphone* and *health* describe hardware or activities the service
>   does not use. The service does not read the customer's location.

**Why not push messaging (Firebase Cloud Messaging)?**

> The app is built without Google Play Services for messaging: order updates are
> delivered through an open-source MQTT broker that we host ourselves, so that the
> same app works on devices and distributions without Google services and no
> order data passes through a third party. That broker delivers a message only to a
> client that is connected at that moment, and a connection cannot be kept open from
> the background without a foreground service.

## Video for the reviewer

The form asks for a short video showing the feature. Record, in one take:

1. Open the app, add an item to the cart, tap **Make order**.
2. Leave the app. Pull down the notification shade and show the ongoing notification
   with the store name, status and progress.
3. From the store's web board, move the order to **Accepted**, then **In route**.
   Show the alert and the ongoing notification changing each time.
4. Finish or cancel the order. Show the ongoing notification being replaced by a
   normal one, and that it can be swiped away.

## Things to keep true

The declaration describes behaviour. If any of these stop being true, update it:

- The service is only started from `CartViewModel` (order placed) and `MainActivity`
  (app opened), through `OrderTrackingLauncher`.
- It always shows an ongoing notification while it runs.
- It stops itself when no order is in progress (`TrackingState.shouldRun`).
- It holds no wake lock and reads no location.

Note: the app does use Google Play Services elsewhere (maps and location for the
address picker). The answer about push messaging above is worded so that it stays
accurate; do not change it to claim the app contains no Google services at all.
