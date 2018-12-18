package xerus.music.mixxx

import xerus.music.mixxx.MixxxDB.connect
import xerus.music.mixxx.data.*
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

/**
 * An object to conveniently and typesafely access the mixxxdb.
 *
 * Call [connect] before doing anything else, it will also return you a [Connection] instance
 * in case you want to query the database directly.
 */
object MixxxDB {
	private lateinit var db: Connection
	
	/** The estimated size of the library. Will be used to construct ArrayLists with an initial capacity to reduce garbage */
	var estimatedLibrarySize = 2048
	
	fun connect(path: String = File(System.getProperty("user.home"), ".mixxx").resolve("mixxxdb.sqlite").toString()): Connection {
		db = DriverManager.getConnection("jdbc:sqlite:$path")
		return db
	}
	
	fun query(query: String): ResultSet = db.createStatement().executeQuery(query)
	
	fun update(query: String) = db.createStatement().executeUpdate(query)
	
	fun update(table: String, update: String, condition: String) = update("UPDATE $table set $update WHERE $condition")
	
	fun deleteFrom(table: String, condition: String) = update("DELETE FROM $table WHERE $condition")
	
	fun <T> readTable(table: String, filter: String?, estimatedSize: Int = 64, converter: ResultSet.() -> T): List<T> {
		val list = ArrayList<T>(estimatedSize)
		query("SELECT * FROM $table${filter?.let { " WHERE $it" } ?: ""}").forEach { list.add(converter(this)) }
		return list
	}
	
	fun getCues(filter: String? = null) = readTable("cues", filter, estimatedLibrarySize * 2) {
		Cue(
			getLong("id"),
			getLong("track_id"),
			getLong("type"),
			getLong("position"),
			getLong("length"),
			getLong("hotcue"),
			getString("label"),
			getLong("color")
		)
	}
	
	fun getCrates(filter: String? = null) = readTable("crates", filter) {
		Crate(
			getLong("id"),
			getString("name"),
			getLong("count"),
			getLong("show"),
			getLong("locked"),
			getLong("autodj_source")
		)
	}
	
	/** Returns a map where the index is the [Track.id] and the value is the [Crate.id] */
	fun getCrateTracks(filter: String? = null) = query("SELECT * FROM crate_tracks${filter?.let { " WHERE $it" } ?: ""}").asIterable().associate {
		Pair(
			it.getLong("track_id"),
			it.getLong("crate_id")
		)
	}
	
	fun getDirectories(filter: String? = null) = readTable("directories", filter) { getString("directory") }
	
	fun getLibrary(filter: String? = null) = readTable("library", filter, estimatedLibrarySize) {
			Track(
				getLong("id"),
				getString("artist"),
				getString("title"),
				getString("album"),
				getString("year"),
				getString("genre"),
				getString("tracknumber"),
				getLong("location"),
				getString("comment"),
				getString("url"),
				getLong("duration"),
				getLong("bitrate"),
				getLong("samplerate"),
				getLong("cuepoint"),
				getDouble("bpm"),
				getLong("channels"),
				getString("datetime_added"),
				getInt("mixxx_deleted") > 0,
				getLong("played"),
				getLong("header_parsed"),
				getString("filetype"),
				getDouble("replaygain"),
				getLong("timesplayed"),
				getLong("rating"),
				getString("key"),
				getString("beats_version"),
				getString("composer"),
				getLong("bpm_lock"),
				getString("beats_sub_version"),
				getString("keys_version"),
				getString("keys_sub_version"),
				getString("key_id"),
				getString("grouping"),
				getString("album_artist"),
				getLong("coverart_source"),
				getLong("coverart_type"),
				getString("coverart_location"),
				getLong("coverart_hash"),
				getDouble("replaygain_peak"),
				getString("tracktotal")
			)
		}
	
	fun getLibraryHashes(filter: String? = null) = readTable("library_hashes", filter, estimatedLibrarySize) {
		LibraryHash(
			getString("directory_path"),
			getLong("hash"),
			getLong("directory_deleted"),
			getLong("needs_verification")
		)
	}
	
	fun getPlaylists(filter: String? = null) = readTable("playlists", filter) {
		Playlist(
			getLong("id"),
			getString("name"),
			getLong("position"),
			getLong("hidden"),
			getTimestamp("date_created"),
			getTimestamp("date_modified"),
			getLong("locked")
		)
	}
	
	fun getPlaylistTracks(filter: String? = null) = readTable("playlist_tracks", filter, estimatedLibrarySize) {
		PlaylistTrack(
			getLong("id"),
			getLong("playlist_id"),
			getLong("track_id"),
			getLong("position"),
			getString("pl_datetime_added")
		)
	}
	
	fun getTrackLocations(filter: String? = null) = readTable("track_locations", filter, estimatedLibrarySize) {
		TrackLocation(
			getLong("id"),
			getString("location"),
			getString("filename"),
			getString("directory"),
			getLong("filesize"),
			getLong("fs_deleted"),
			getLong("needs_verification")
		)
	}
	
}

inline fun ResultSet.forEach(function: ResultSet.() -> Unit) {
	while (next())
		function(this)
}

fun ResultSet.asIterable() =
	Iterable {
		object : Iterator<ResultSet> {
			var advanced = false
			var hasNext = true
			override fun hasNext(): Boolean {
				if (!advanced) {
					hasNext = this@asIterable.next()
					advanced = true
				}
				return hasNext
			}
			
			override fun next(): ResultSet {
				if (!advanced)
					this@asIterable.next()
				advanced = false
				return this@asIterable
			}
			
		}
	}