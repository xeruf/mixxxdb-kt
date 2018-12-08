package xerus.music.mixxx.data

import java.sql.Timestamp

data class Playlist(
	val id: Long,
	val name: String?,
	val position: Long,
	val hidden: Long,
	val dateCreated: Timestamp?,
	val dateModified: Timestamp?,
	val locked: Long
)