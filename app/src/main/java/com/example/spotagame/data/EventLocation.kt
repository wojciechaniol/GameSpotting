package com.example.spotagame.data

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.google.android.gms.maps.model.LatLng

@Keep
data class EventLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val name: String = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
        latitude = parcel.readDouble(),
        longitude = parcel.readDouble(),
        name = parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeString(name)
    }

    override fun describeContents(): Int = 0

    fun toLatLng() = LatLng(latitude, longitude)

    companion object CREATOR : Parcelable.Creator<EventLocation> {
        override fun createFromParcel(parcel: Parcel): EventLocation = EventLocation(parcel)
        override fun newArray(size: Int): Array<EventLocation?> = arrayOfNulls(size)
    }
}
