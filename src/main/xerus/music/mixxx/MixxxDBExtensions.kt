package xerus.music.mixxx

import xerus.music.mixxx.data.*

fun MixxxDB.getTracksIndexed() = getLibrary().associateBy { it.id }

fun MixxxDB.getTrackLocationsIndexed() = getTrackLocations().associateBy { it.id }

fun MixxxDB.getCratesIndexed() = getCrates().associateBy { it.id }

fun MixxxDB.getPlaylistsIndexed() = getPlaylists().associateBy { it.id }

/** Returns a Map containing the id of all Crates and the Tracks that belong to them */
fun MixxxDB.crateIdsToTracks(): Map<Long, List<Track>> {
	val tracks = getTracksIndexed()
	val crateTracks = getCrateTracks()
	val crates = getCrates()
	val idsToTracks = HashMap<Long, ArrayList<Track>>(crates.size)
	crates.forEach { crate -> idsToTracks[crate.id] = ArrayList() }
	crateTracks.forEach { trackId, crateId ->
		idsToTracks[crateId]!!.add(tracks[trackId]!!)
	}
	return idsToTracks
}

/** Returns a Map containing all Crates and the Tracks that belong to them */
fun MixxxDB.cratesToTracks(): Map<Crate, List<Track>> {
	val crates = getCratesIndexed()
	val cratesToTracks = HashMap<Crate, List<Track>>(crates.size)
	crateIdsToTracks().forEach { crateId, trackList ->
		cratesToTracks[crates[crateId]!!] = trackList
	}
	return cratesToTracks
}

/** Returns a Map containing the id of all Playlists and the Tracks that belong to them in their correct order */
fun MixxxDB.playlistIdsToTracks(): Map<Long, List<Track>> {
	val tracks = getTracksIndexed()
	val playlistTracks = getPlaylistTracks()
	val playlists = getPlaylists()
	val idsToList = HashMap<Long, ArrayList<PlaylistTrack>>(playlists.size)
	playlists.forEach { crate -> idsToList[crate.id] = ArrayList() }
	playlistTracks.forEach { playlistTrack ->
		idsToList[playlistTrack.playlistId]!!.add(playlistTrack)
	}
	val idsToListSorted = HashMap<Long, List<Track>>(playlists.size)
	idsToList.forEach { id, trackList ->
		idsToListSorted[id] = trackList.sorted().map { tracks[it.trackId]!! }
	}
	return idsToListSorted
}

/** Returns a Map containing all Playlists and the Tracks that belong to them in their correct order */
fun MixxxDB.playlistsToTracks(): Map<Playlist, List<Track>> {
	val playlists = getPlaylistsIndexed()
	val playlistsToTracks = HashMap<Playlist, List<Track>>(playlists.size)
	playlistIdsToTracks().forEach { playlistId, trackList ->
		playlistsToTracks[playlists[playlistId]!!] = trackList
	}
	return playlistsToTracks
}

fun MixxxDB.getTrackIdsToCues(): Map<Long, List<Cue>> {
	val cues = getCues()
	val map = HashMap<Long, ArrayList<Cue>>()
	cues.forEach {
		map.getOrPut(it.trackId) { ArrayList() }.add(it)
	}
	return map
}

fun MixxxDB.getTracksToCues(): Map<Track, List<Cue>> {
	val tracks = getTracksIndexed()
	val map = HashMap<Track, List<Cue>>()
	getTrackIdsToCues().forEach { id, cues ->
		tracks[id]?.let { map.put(it, cues) }
	}
	return map
}