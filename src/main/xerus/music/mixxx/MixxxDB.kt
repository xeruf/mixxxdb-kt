package xerus.music.mixxx

import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

object MixxxDB {
	
	private lateinit var db: Connection
	fun connect(path: String = File(System.getProperty("user.home"), ".mixxx").resolve("mixxxdb.sqlite").toString()) {
		db = DriverManager.getConnection("jdbc:sqlite:$path")
	}
	
	fun query(query: String): ResultSet {
		val statement = db.createStatement()
		return statement.executeQuery(query)
	}
	
	fun <T> readTable(table: String, estimatedSize: Int = 2000, converter: ResultSet.() -> T): List<T> {
		val results = query("SELECT * FROM $table")
		val list = ArrayList<T>(estimatedSize)
		while (results.next())
			list.add(converter(results))
		return list
	}
	
	fun getLibrary(): List<Song> =
		readTable("library") {
			Song(
				getInt("id"),
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
	
	fun getCues() = readTable("cues") {
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
	
}

data class Song(
	val id: Int,
	val artist: String?,
	val title: String?,
	val album: String?,
	val year: String?,
	val genre: String?,
	val tracknumber: String?,
	val location: Long,
	val comment: String?,
	val url: String?,
	val duration: Long,
	val bitrate: Long,
	val samplerate: Long,
	val cuepoint: Long,
	val bpm: Double,
	val channels: Long,
	val datetimeAdded: String?,
	val mixxx_deleted: Boolean,
	val played: Long,
	val headerParsed: Long,
	val filetype: String?,
	val replaygain: Double,
	val timesplayed: Long,
	val rating: Long,
	val key: String?,
	val beatsVersion: String?,
	val composer: String?,
	val bpmLock: Long,
	val beatsSubVersion: String?,
	val keysVersion: String?,
	val keysSubVersion: String?,
	val keyId: String?,
	val grouping: String?,
	val albumArtist: String?,
	val coverartSource: Long,
	val coverartType: Long,
	val coverartLocation: String?,
	val coverartHash: Long,
	val replaygainPeak: Double,
	val tracktotal: String?
)

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