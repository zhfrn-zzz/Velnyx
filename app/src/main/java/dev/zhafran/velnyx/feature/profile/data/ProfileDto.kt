package dev.zhafran.velnyx.feature.profile.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileDto(
    val id: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("weight_kg") val weightKg: Double,
    @SerialName("units_pref") val unitsPref: String = "metric",
)

fun ProfileDto.toDomain(): Profile = Profile(
    id = id,
    displayName = displayName,
    weightKg = weightKg,
)
