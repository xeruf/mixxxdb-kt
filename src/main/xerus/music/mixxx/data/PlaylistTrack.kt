package xerus.music.mixxx.data

data class PlaylistTrack(
	val id: Long,
	val playlistId: Long,
	val trackId: Long,
	val position: Long,
	val plDatetimeAdded: String?
): Comparable<PlaylistTrack> {
	override fun compareTo(other: PlaylistTrack): Int = (position - other.position).toInt()
}