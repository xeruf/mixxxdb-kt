package xerus.music.mixxx.data

import java.util.*

data class Playlist(
	val id: Long,
	val name: String?,
	val position: Long,
	val hidden: Long,
	val dateCreated: Date,
	val dateModified: Date,
	val locked: Long
)