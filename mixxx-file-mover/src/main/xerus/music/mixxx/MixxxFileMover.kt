package xerus.music.mixxx

import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.collections.transformation.SortedList
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Paint
import javafx.stage.FileChooser
import mu.KotlinLogging
import xerus.ktutil.containsAny
import xerus.ktutil.findFolder
import xerus.ktutil.javafx.MenuItem
import xerus.ktutil.javafx.TableColumn
import xerus.ktutil.javafx.bind
import xerus.ktutil.javafx.bindText
import xerus.ktutil.javafx.grow
import xerus.ktutil.javafx.onClick
import xerus.ktutil.javafx.properties.SimpleObservable
import xerus.ktutil.javafx.properties.addListener
import xerus.ktutil.javafx.properties.listen
import xerus.ktutil.javafx.selectedItem
import xerus.ktutil.javafx.ui.App
import xerus.ktutil.javafx.ui.createAlert
import xerus.ktutil.nullIfEmpty
import xerus.music.mixxx.data.Track
import xerus.music.mixxx.data.TrackLocation
import java.io.File
import java.util.function.Predicate

fun main(args: Array<String>) {
	MixxxDB.connect()
	MixxxFileMover.updateDatabase()
	MixxxFileMover.start()
}

private val logger = KotlinLogging.logger { }

object MixxxFileMover {
	
	val locTracks: ObservableList<LocTrack> = FXCollections.observableArrayList<LocTrack>()
	val library: ObservableList<Track> = FXCollections.observableArrayList<Track>()
	val cues = MixxxDB.getTracksToCues()
	
	fun updateDatabase() {
		library.setAll(MixxxDB.getLibrary())
		val locs = MixxxDB.getTrackLocations()
		locTracks.setAll(locs.map { loc ->
			var cueAmount: Int? = null
			cues.forEach { track, cues ->
				if(track.location == loc.id)
					cueAmount = cues.size
			}
			LocTrack(loc, cueAmount)
		})
	}
	
	fun start() {
		App.launch("Mixxx Mover") {
			
			val locPredicate = SimpleObservable<Predicate<LocTrack>>(Predicate { true })
			val predicate = SimpleObservable<Predicate<Track>>(Predicate { true })
			val locationTable = TableView(locTracks).filtered(locPredicate)
			locationTable.columnsFromProperties { it.loc }
			locationTable.columns.add(TableColumn<LocTrack, Int?>("Cues") { it.value.cues })
			
			locationTable.contextMenu = ContextMenu(
				MenuItem("Choose new location") {
					chooseNewLocation(locationTable.selectedItem ?: return@MenuItem)
				},
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
			
			val findField = TextField().bindText(Settings.FIND)
			val replaceButton = Button("Replace All")
			replaceButton.setOnAction {
				replace(locationTable.selectionModel.selectedItems.nullIfEmpty() ?: locationTable.items)
				updateDatabase()
			}
			val replaceRow = HBox(Label("Find"), findField,
				Label("Replace by"), TextField().bindText(Settings.REPLACE),
				CheckBox("Case-sensitive").bind(Settings.CASESENSITIVE),
				replaceButton,
				Button("Reload").onClick { updateDatabase() }
			)
			
			arrayOf(Settings.FIND, Settings.CASESENSITIVE).addListener(true) {
				val search = Settings.FIND()
				val error = try {
					when(search) {
						"IS:DUPLICATE" -> {
							locPredicate.value = Predicate { track -> locTracks.filter { it.location == track.location }.size > 1 }
							predicate.value = Predicate { track -> library.filter { it.artist == track.artist && it.title == track.title && it.album == track.album }.size > 1 }
						}
						"IS:LOCATIONDUPLICATE" -> {
							predicate.value = Predicate { track -> library.filter { it.location == track.location }.size > 1 }
							locPredicate.value = Predicate { track -> track.loc.location.contains(searchRegex()) }
						}
						else -> {
							val regex = searchRegex()
							locPredicate.value = Predicate { track -> track.loc.location.contains(regex) }
							predicate.value = Predicate { track -> arrayOf(track.artist, track.albumArtist, track.title, track.album).joinToString(" - ") { it.orEmpty() }.contains(regex) }
						}
					}
					false
				} catch(t: Throwable) {
					true
				}
				findField.border = Border(BorderStroke(Paint.valueOf(if(error) "red" else "#00000000"), BorderStrokeStyle.SOLID, CornerRadii(4.0), BorderWidths.DEFAULT))
				replaceButton.isDisable = search.isEmpty() || error
			}
			
			val tabPane = TabPane(*tabs)
			tabPane.tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
			Scene(VBox(replaceRow, tabPane.grow()), 2400.0, 1500.0)
		}
	}
	
	fun chooseNewLocation(selected: LocTrack) {
		val newLocation = FileChooser().apply {
			title = "Choose new location"
			initialDirectory = File(selected.loc.directory).findFolder()
		}.showOpenDialog(App.stage) ?: return
		tryLocationUpdate(selected, newLocation)
	}
	
	private fun tryLocationUpdate(selected: LocTrack, newLocation: File) {
		try {
			updateLocation(selected.loc.id, newLocation)
			updateDatabase()
		} catch(e: Exception) {
			if(e.message?.contains("SQLITE_CONSTRAINT_UNIQUE") == true)
				App.stage.createAlert(Alert.AlertType.WARNING, title = "Relocation error", header = "A track with this location already exists",
					content = "Would you like to override it? Cues: ${selected.cues} Other: ${locTracks.find { it.location == newLocation }?.cues}", buttons = *arrayOf(ButtonType.YES, ButtonType.NO)).apply {
					showAndWait()
					resultProperty().listen {
						if(it == ButtonType.YES) {
							val locs = MixxxDB.getTrackLocations("location = \"$newLocation\"").map {
								println(it)
								it
							}
							MixxxDB.deleteFrom("track_locations", "location = \"$newLocation\"")
							MixxxDB.deleteFrom("library", "location IN ${locs.map { it.id }.joinToString(",", "(", ")")}")
							tryLocationUpdate(selected, newLocation)
						}
					}
				}
			else
				App.stage.createAlert(Alert.AlertType.ERROR, title = "Relocation error", content = e.toString(), buttons = *arrayOf(ButtonType.OK)).show()
		}
	}
	
}

data class LocTrack(val loc: TrackLocation, val cues: Int?) {
	val location = File(loc.location)
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