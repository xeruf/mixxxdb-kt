package xerus.music.mixxx.data

data class LibraryHash(
	val directoryPath: String?,
	val hash: Long,
	val directoryDeleted: Long,
	val needsVerification: Long
)