package xerus.music.mixxx.data

data class Crate(
	val id: Long,
	val name: String?,
	val count: Long,
	val show: Long,
	val locked: Long,
	val autodjSource: Long
)