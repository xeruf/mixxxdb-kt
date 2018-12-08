package xerus.music.mixxx.data

data class TrackLocation(
	val id: Long,
	val location: String,
	val filename: String,
	val directory: String,
	val filesize: Long,
	val fsDeleted: Long,
	val needsVerification: Long
)