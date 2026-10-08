package id.andreasmlbngaol.mpus.core.domain.model

/** A latitude/longitude pair. Domain-level, so no Android `Location` leaks out of data. */
data class GeoPoint(val lat: Double, val lng: Double)
