package com.app.koratime.notification.model;

public enum NotificationType {
    BOOKING_REQUESTED,        // → manager, when a player submits a booking
    BOOKING_ACCEPTED,         // → player, when manager accepts
    BOOKING_REJECTED,         // → player, when manager rejects
    BOOKING_AUTO_EXPIRED,     // → player, when scheduler times out a pending request
    BOOKING_CANCELLED,        // → the other party, when either side cancels an accepted booking
    WAITLIST_SLOT_AVAILABLE   // → player, when a slot they were waitlisted for opens up

}
