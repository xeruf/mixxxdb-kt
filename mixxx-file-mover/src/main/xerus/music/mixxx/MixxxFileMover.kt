package xerus.music.mixxx

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
import xerus.ktutil.findFolder
import xerus.ktutil.javafx.*
import xerus.ktutil.javafx.properties.addListener
import xerus.ktutil.javafx.properties.bindSoft
import xerus.ktutil.javafx.ui.App
import xerus.ktutil.javafx.ui.createAlert
import xerus.music.mixxx.data.TrackLocation
import java.io.File
import java.util.function.Predicate

fun main(args: Array<String>) {
	MixxxDB.connect()
	MixxxFileMover.updateDatabase()
	MixxxFileMover.launch()
}

private val logger = KotlinLogging.logger { }

object MixxxFileMover {
	
	val tracks: ObservableList<Track> = FXCollections.observableArrayList<Track>()
	val cues = MixxxDB.getTracksToCues()
	
	fun updateDatabase() {
		val locs = MixxxDB.getTrackLocations()
		tracks.setAll(locs.map { loc ->
			var cueAmount = 0
			cues.forEach { track, cues ->
				if(track.location == loc.id)
					cueAmount = cues.size
			}
			Track(loc, cueAmount)
		})
	}
	
	fun launch() {
		App.launch("Mixxx Mover") {
			// todo searchRow
			val filteredItems = FilteredList(tracks)
			val findField = TextField().bindText(Settings.FIND)
			val replaceRow = HBox(Label("Find"), findField, Label("Replace by"), TextField().bindText(Settings.REPLACE), CheckBox("Case-sensitive").bind(Settings.CASESENSITIVE))
			arrayOf(Settings.FIND, Settings.CASESENSITIVE).addListener(true) {
				var color = try {
					val regex = Regex(Settings.FIND(), if(Settings.CASESENSITIVE()) setOf() else setOf(RegexOption.IGNORE_CASE))
					filteredItems.setPredicate { track -> track.loc.location.contains(regex) }
					"#00000000"
				} catch(t: Throwable) {
					"red"
				}
				findField.border = Border(BorderStroke(Paint.valueOf(color), BorderStrokeStyle.SOLID, CornerRadii(4.0), BorderWidths.DEFAULT))
			}
			val tableItems = SortedList(filteredItems)
			val table = TableView(tableItems)
			tableItems.comparatorProperty().bind(table.comparatorProperty())
			table.columns.addAll(TrackLocation::class.java.declaredMethods.filter { it.name.startsWith("get") }.map { method ->
				TableColumn<Track, Any?>(method.name.substring(3)) {
					method.invoke(it.value.loc)
				}
			})
			table.columns.add(TableColumn<Track, Int>("Cues") { it.value.cues })
			table.columns.removeIf { it.text in arrayOf("NeedsVerification", "Filesize") }
			table.columns.sortBy {
				when(it.text) {
					"Id" -> 0
					
					"Artist" -> 1
					"Title" -> 2
					"Album" -> 3
					"Tracknumber" -> 4
					
					"FsDeleted" -> 5
					"Filename" -> 6
					"Directory" -> 7
					"Location" -> 8
					
					else -> 9999
				}
			}
			table.contextMenu = ContextMenu(
				MenuItem("Choose new location") {
					chooseNewLocation(table.selectedItem ?: return@MenuItem)
				},
				MenuItem("Delete") {
					delete(table.selectionModel.selectedItems?.map { it.loc.id } ?: return@MenuItem)
					updateDatabase()
				},
				MenuItem("Replace") {
					update(table.selectionModel.selectedItems ?: return@MenuItem) { it }
					updateDatabase()
				}
			)
			table.setOnMouseClicked { me ->
				if(me.clickCount == 2)
					chooseNewLocation(table.selectedItem ?: return@setOnMouseClicked)
			}
			table.selectionModel.selectionMode = SelectionMode.MULTIPLE
			Scene(VBox(replaceRow, table.grow()), 1800.0, 800.0)
		}
	}
	
	fun chooseNewLocation(selected: Track) {
		val newLocation = FileChooser().apply {
			title = "Choose new location"
			initialDirectory = File(selected.loc.directory).findFolder()
		}.showOpenDialog(App.stage) ?: return
		tryLocationUpdate(selected, newLocation)
	}
	
	private fun tryLocationUpdate(selected: Track, newLocation: File) {
		try {
			updateLocation(selected.loc.id, newLocation)
		} catch(e: Exception) {
			if(e.message?.contains("SQLITE_CONSTRAINT_UNIQUE") == true)
				App.stage.createAlert(Alert.AlertType.WARNING, title = "Relocation error", header = "A track with this location already exists",
					content = "Would you like to override it? Cues: ${selected.cues} Other: ${tracks.find { it.location == newLocation }?.cues}", buttons = *arrayOf(ButtonType.YES, ButtonType.NO)).apply {
					showAndWait()
					if(result == ButtonType.YES) {
						MixxxDB.update("DELETE FROM track_locations WHERE location = $newLocation")
						tryLocationUpdate(selected, newLocation)
					}
				}
			else
				App.stage.createAlert(Alert.AlertType.ERROR, title = "Relocation error", content = e.toString(), buttons = *arrayOf(ButtonType.OK)).show()
			return
		}
		updateDatabase()
	}
	
}

data class Track(val loc: TrackLocation, val cues: Int) {
	val location = File(loc.location)
}

private fun updateLocation(id: Long, newLocation: File) {
	logger.debug("Setting location for $id to $newLocation")
	MixxxDB.update("""UPDATE track_locations SET fs_deleted = 0, filename = "${newLocation.name}", directory = "${newLocation.parent}", location = "$newLocation" WHERE id LIKE $id""")
}

private fun delete(ids: Collection<Long>) {
	logger.debug("Deleting $ids")
	MixxxDB.update("DELETE FROM track_locations WHERE id IN ${ids.joinToString(",", "(", ")")}")
}

private fun delete(id: Long) {
	logger.debug("Deleting $id")
	MixxxDB.update("DELETE FROM track_locations WHERE id = $id")
}

private fun update(list: List<Track>, update: (File) -> File) {
	list.forEach { track -> updateLocation(track.loc.id, update(track.location)) }
}
