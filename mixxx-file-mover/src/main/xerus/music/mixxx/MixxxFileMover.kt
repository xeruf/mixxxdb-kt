package xerus.music.mixxx

import ch.qos.logback.classic.Level
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.collections.transformation.SortedList
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Paint
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import xerus.ktutil.collections.nullIfEmpty
import xerus.ktutil.containsAny
import xerus.ktutil.containsEach
import xerus.ktutil.findExistingDirectory
import xerus.ktutil.calculateSimilarity
import xerus.ktutil.helpers.Named
import xerus.ktutil.javafx.*
import xerus.ktutil.javafx.properties.ImmutableObservableList
import xerus.ktutil.javafx.properties.SimpleObservable
import xerus.ktutil.javafx.properties.addListener
import xerus.ktutil.javafx.properties.listen
import xerus.ktutil.javafx.ui.App
import xerus.ktutil.javafx.ui.createAlert
import xerus.ktutil.javafx.ui.onConfirm
import xerus.ktutil.javafx.ui.resize
import xerus.ktutil.splitTitleTrimmed
import xerus.music.mixxx.data.Track
import xerus.music.mixxx.data.TrackLocation
import java.io.File
import java.util.function.Predicate

fun main(args: Array<String>) {
	args.indexOf("--loglevel").takeIf { it > -1 }?.let {
		logLevel = args.getOrNull(it + 1)?.let { Level.toLevel(it, null) } ?: run {
			println("WARNING: Loglevel argument given without a valid value! Use one of {OFF, ERROR, WARN, INFO, DEBUG, TRACE, ALL}")
			return@let
		}
	}
	MixxxDB.connect()
	MixxxFileMover.updateDatabase()
	MixxxFileMover.start()
}

typealias FileMatcher = (File) -> Double

object MixxxFileMover {
	
	val locTracks: ObservableList<LocTrack> = FXCollections.observableArrayList<LocTrack>()
	val library: ObservableList<Track> = FXCollections.observableArrayList<Track>()
	val cues = MixxxDB.getTracksToCues()
	
	fun updateDatabase() {
		library.setAll(MixxxDB.getLibrary())
		val locs = MixxxDB.getTrackLocations()
		locTracks.setAll(locs.map { loc ->
			var found = false
			var cueAmount: Int? = null
			var bpmLock: Boolean? = null
			cues.forEach { (track, cues) ->
				if(track.location == loc.id) {
					found = true
					cueAmount = cues.size
					bpmLock = track.bpmLock
				}
			}
			if(!found)
				library.forEach { track ->
					if(track.location == loc.id) {
						found = true
						cueAmount = 0
						bpmLock = track.bpmLock
					}
				}
			LocTrack(loc, cueAmount, bpmLock)
		})
	}
	
	fun start() {
		App.launch("Mixxx Mover") {
			val locPredicates = FXCollections.observableArrayList<Predicate<LocTrack>>(Predicate { true })
			val predicates = FXCollections.observableArrayList<Predicate<Track>>(Predicate { true })
			
			val findField = TextField().bindText(Settings.FIND)
			val replaceButton = Button("Replace All")
			val replaceRow = HBox(
				Label("Find"), findField,
				Label("Replace by"), TextField().bindText(Settings.REPLACE),
				CheckBox("Case-sensitive").bind(Settings.CASESENSITIVE),
				replaceButton,
				Button("Reload").onClick { updateDatabase() },
				ComboBox<Filter>(ImmutableObservableList(*Filter.values())).apply {
					this.selectionModel.selectedItemProperty().addListener { _, old, new ->
						predicates.remove(old?.filterTrack)
						locPredicates.remove(old?.filterLoc)
						predicates.add(new.filterTrack)
						locPredicates.add(new.filterLoc)
					}
				}
			)
			
			arrayOf(Settings.FIND, Settings.CASESENSITIVE).addListener(true) {
				val search = Settings.FIND()
				val error = try {
					val regex = searchRegex()
					locPredicates[0] = Predicate { track -> track.loc.location.contains(regex) }
					predicates[0] = Predicate { track -> arrayOf(track.artist, track.albumArtist, track.title, track.album).joinToString(" - ") { it.orEmpty() }.contains(regex) }
					false
				} catch(t: Throwable) {
					true
				}
				findField.border = Border(BorderStroke(Paint.valueOf(if(error) "red" else "#00000000"), BorderStrokeStyle.SOLID, CornerRadii(4.0), BorderWidths.DEFAULT))
				replaceButton.isDisable = search.isEmpty() || error
			}
			
			val locPredicate = SimpleObservable<Predicate<LocTrack>>(Predicate { true })
			locPredicates.listen { locPredicate.value = it.reduce { acc, predicate -> acc.and(predicate) } }
			val predicate = SimpleObservable<Predicate<Track>>(Predicate { true })
			predicates.listen { predicate.value = it.reduce { acc, predicate -> acc.and(predicate) } }
			
			val locationTable = TableView(locTracks).filtered(locPredicate)
			locationTable.columnsFromProperties { it.loc }
			locationTable.columns.add(TableColumn<LocTrack, Int?>("Cues") { it.value.cues })
			locationTable.columns.add(TableColumn<LocTrack, Boolean?>("BpmLock") { it.value.bpmLock })
			
			locationTable.contextMenu = ContextMenu(
				MenuItem("Choose new location") {
					chooseNewLocation(locationTable.selectedItem ?: return@MenuItem)
				},
				Menu("Auto-detect new location", null,
					MenuItem("in current directory (rough)") {
						detectNewLocations(locationTable.selectionModel.selectedItems, { original ->
							val list = original.nameWithoutExtension.splitTitleTrimmed()
							return@detectNewLocations { file: File -> calculateSimilarity(list, file.nameWithoutExtension.splitTitleTrimmed()) }
						}) { origin, matcher -> origin.listFiles()?.maxBy(matcher) }
					},
					MenuItem("in current tree") {
						detectNewLocations(locationTable.selectionModel.selectedItems) { origin, matcher -> origin.walkTopDown().maxBy(matcher) }
					},
					MenuItem("in parent tree") {
						detectNewLocations(locationTable.selectionModel.selectedItems) { origin, matcher -> origin.parentFile.walkTopDown().maxBy(matcher) }
					},
					MenuItem("in ...") {
						val dir = DirectoryChooser().apply {
							initialDirectory = locationTable.selectedItem?.location?.findExistingDirectory()
						}.showDialog(App.stage)
						detectNewLocations(locationTable.selectionModel.selectedItems) { origin, matcher -> dir.walkTopDown().maxBy(matcher) }
					},
					MenuItem("in ... (rough)") {
						val dir = DirectoryChooser().apply {
							initialDirectory = locationTable.selectedItem?.location?.findExistingDirectory()
						}.showDialog(App.stage)
						detectNewLocations(locationTable.selectionModel.selectedItems, { original ->
							val list = original.nameWithoutExtension.splitTitleTrimmed()
							return@detectNewLocations { file: File -> calculateSimilarity(list, file.nameWithoutExtension.splitTitleTrimmed()) }
						}) { origin, matcher -> dir.walkTopDown().maxBy(matcher) }
					}
				),
				MenuItem("Delete") {
					val toDelete = locationTable.selectionModel.selectedItems?.map { it.loc.id } ?: return@MenuItem
					delete(toDelete)
					updateDatabase()
				},
				MenuItem("Replace") {
					replace(locationTable.selectionModel.selectedItems ?: return@MenuItem)
					updateDatabase()
				}
			)
			locationTable.setOnMouseClicked { me ->
				if(me.clickCount == 2)
					chooseNewLocation(locationTable.selectedItem ?: return@setOnMouseClicked)
			}
			
			val libraryTable = TableView<Track>(library).filtered(predicate)
			libraryTable.columnsFromProperties()
			libraryTable.columns.add(TableColumn<Track, Int?>("Cues") {
				cues[it.value]?.size
			})
			libraryTable.contextMenu = ContextMenu(
				MenuItem("Delete") {
					val toDelete = libraryTable.selectionModel.selectedItems?.map { it.id } ?: return@MenuItem
					delete(toDelete, "library")
					updateDatabase()
				}
			)
			
			val tabs = arrayOf("Locations" to locationTable, "Library" to libraryTable).map { pair ->
				pair.second.columns.apply {
					removeIf { it.text.containsAny("NeedsVerification", "CoverArt", "Samplerate", "Key", "Version", "Wavesummaryhex", "Hash") }
					sortBy { column ->
						when(column.text) {
							"Id" -> 0
							"Cues" -> 1
							
							"Artist" -> 11
							"Title" -> 12
							"Album" -> 13
							"Tracknumber" -> 14
							
							"FsDeleted" -> 11
							"Filename" -> 12
							"Directory" -> 13
							"Location" -> 14
							
							"Filesize" -> 1001
							
							else -> 999
						}
					}
				}
				pair.second.selectionModel.selectionMode = SelectionMode.MULTIPLE
				Tab(pair.first, StackPane(pair.second))
			}.toTypedArray()
			
			replaceButton.setOnAction {
				replace(locationTable.selectionModel.selectedItems.nullIfEmpty() ?: locationTable.items)
				updateDatabase()
			}
			
			val tabPane = TabPane(*tabs)
			tabPane.tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
			Scene(VBox(replaceRow, tabPane.grow()), 2400.0, 1500.0)
		}
	}
	
	fun chooseNewLocation(selected: LocTrack) {
		val newLocation = FileChooser().apply {
			title = "Choose new location"
			initialDirectory = File(selected.loc.directory).findExistingDirectory()
		}.showOpenDialog(App.stage) ?: return
		tryLocationUpdate(selected, newLocation)
	}
	
	private fun tryLocationUpdate(selected: LocTrack, newLocation: File) {
		try {
			updateLocation(selected.loc.id, newLocation)
			updateDatabase()
		} catch(e: Exception) {
			if(e.message?.contains("SQLITE_CONSTRAINT_UNIQUE") == true) {
				val other = locTracks.find { it.location == newLocation }
				if(other != null && other.cues == 0 && other.bpmLock == false)
					retryLocationUpdate(selected, newLocation)
				else
					App.stage.createAlert(Alert.AlertType.WARNING, "Relocation error", "A track with this location already exists",
						"Would you like to override it? Cues: ${selected.cues} Other: ${other?.cues}",
						ButtonType.YES, ButtonType.NO)
						.onConfirm { retryLocationUpdate(selected, newLocation) }
			} else {
				App.stage.createAlert(Alert.AlertType.ERROR, title = "Relocation error", content = e.toString(), buttons = *arrayOf(ButtonType.OK)).show()
			}
		}
	}
	
	private fun retryLocationUpdate(selected: LocTrack, newLocation: File) {
		val locs = MixxxDB.getTrackLocations("location = \"$newLocation\"").map {
			logger.debug("Moving $it and deleting conflicting entries")
			it
		}
		MixxxDB.deleteFrom("track_locations", "location = \"$newLocation\"")
		MixxxDB.deleteFrom("library", locs.map { it.id }.sqlFilter("location"))
		tryLocationUpdate(selected, newLocation)
	}
	
	fun detectNewLocations(items: Collection<LocTrack>, match: (File) -> FileMatcher = ::createMatcher, walker: (File, (File) -> Double) -> File?) {
		val results: List<Pair<LocTrack, File>>? = items.mapNotNull { track ->
			val matchFile = match(track.location)
			walker(File(track.loc.directory)) { if(it.isFile) matchFile(it) else Double.MIN_VALUE }
				?.let { Pair(track, it) }
		}.nullIfEmpty()
		App.stage.createAlert(Alert.AlertType.CONFIRMATION, "Confirm moves (${results?.size}/${items.size})", null,
			results?.joinToString("\n") { "${it.first.loc.filename} to ${it.second.relativeTo(it.first.location.parentFile)}" }
				?: "Nothing found!", ButtonType.OK, ButtonType.NO)
			.resize(900.0)
			.onConfirm {
				results?.forEach {
					tryLocationUpdate(it.first, it.second)
				}
			}
	}
	
	fun createMatcher(original: File): FileMatcher {
		val name = original.nameWithoutExtension
		val ext = original.extension
		return { file: File -> if(file.extension == ext && file.nameWithoutExtension.containsEach(name)) 1.0 else 0.0 }
	}
}

data class LocTrack(val loc: TrackLocation, val cues: Int?, val bpmLock: Boolean?) {
	val location = File(loc.location)
}

enum class Filter(val filterTrack: Predicate<Track>, val filterLoc: Predicate<LocTrack>): Named {
	None(Predicate { true }, Predicate { true }),
	Duplicate(Predicate { track -> MixxxFileMover.library.filter { it.artist == track.artist && it.title == track.title && it.album == track.album }.size > 1 }, Predicate { track -> MixxxFileMover.locTracks.filter { it.location == track.location }.size > 1 }),
	LocationDuplicate(Predicate { track -> MixxxFileMover.library.filter { it.location == track.location }.size > 1 }, Predicate { track -> MixxxFileMover.locTracks.filter { it.location == track.location }.size > 1 }),
	Missing(Predicate { it.mixxx_deleted }, Predicate { it.loc.fsDeleted == 1L }) ;
	
	override val displayName: String
		get() = name
}

private fun updateLocation(id: Long, newLocation: File) {
	logger.debug("Setting location of $id to $newLocation")
	MixxxDB.update("""UPDATE track_locations SET fs_deleted = 0, filename = "${newLocation.name}", directory = "${newLocation.parent}", location = "$newLocation" WHERE id = $id""")
}

private fun delete(ids: Collection<Long>, table: String = "track_locations") {
	logger.debug("Deleting $ids from $table")
	MixxxDB.deleteFrom(table, ids.sqlFilter("id"))
	if(table == "track_locations") {
		logger.debug("Deleting tracks with location $ids from library")
		MixxxDB.deleteFrom("library", ids.sqlFilter("location"))
	}
}

private fun searchRegex() = Regex(Settings.FIND(), if(Settings.CASESENSITIVE()) setOf() else setOf(RegexOption.IGNORE_CASE))

private fun replace(list: List<LocTrack>) =
	update(list) { File(it.toString().replace(searchRegex(), Settings.REPLACE())) }

private fun update(list: List<LocTrack>, update: (File) -> File) {
	list.forEach { track -> updateLocation(track.loc.id, update(track.location)) }
}

fun <T> TableView<T>.filtered(filter: ObservableValue<Predicate<T>>): TableView<T> {
	val filtered = FilteredList(this.items)
	filtered.predicateProperty().bind(filter)
	items = SortedList(filtered).also { it.comparatorProperty().bind(this.comparatorProperty()) }
	return this
}

inline fun <reified T> TableView<T>.columnsFromProperties() {
	columns.addAll(T::class.java.declaredMethods.filter { it.name.startsWith("get") }.map { method ->
		TableColumn<T, Any?>(method.name.substring(3)) {
			method.invoke(it.value)
		}
	})
}

inline fun <T, reified U> TableView<T>.columnsFromProperties(crossinline converter: (T) -> U) {
	columns.addAll(U::class.java.declaredMethods.filter { it.name.startsWith("get") }.map { method ->
		TableColumn<T, Any?>(method.name.substring(3)) {
			method.invoke(converter(it.value))
		}
	})
}

fun Collection<*>.sqlFilter(columnName: String) = "$columnName IN ${joinToString(",", "(", ")")}"