package xerus.music.mixxx.data

data class Cue(
	val id: Long,
	val trackId: Long,
	val type: Long,
	val position: Long,
	val length: Long,
	val hotcue: Long,
	val label: String?,
	val color: Long
)