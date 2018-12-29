package xerus.music.mixxx

fun main(args: Array<String>) {
	MixxxDB.connect()
	val map = MixxxDB.playlistIdsToTrackIds()
	map.forEach { id, tracks ->
		if(tracks.size < 3) {
			println("Deleting $id " + tracks.size)
			MixxxDB.deleteFrom("Playlists", "id IS $id")
			MixxxDB.deleteFrom("PlaylistTracks", "playlist_id IS $id")
		}
	}
}