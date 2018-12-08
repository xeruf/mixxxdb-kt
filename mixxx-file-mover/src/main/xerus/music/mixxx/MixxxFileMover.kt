package xerus.music.mixxx

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.scene.Scene
import javafx.scene.control.ContextMenu
import javafx.scene.control.Label
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import xerus.ktutil.findFolder
import xerus.ktutil.javafx.*
import xerus.ktutil.javafx.properties.dependOn
import xerus.ktutil.javafx.ui.App
import xerus.music.mixxx.data.TrackLocation
import java.io.File
import java.util.function.Predicate

fun main(args: Array<String>) {
	MixxxDB.connect()
	MixxxFileMover.updateDatabase()
	MixxxFileMover.launch()
}

object MixxxFileMover {
	
	val items: ObservableList<Track> = FXCollections.observableArrayList<Track>()
	val cues = MixxxDB.getTracksToCues()
	
	fun updateDatabase() {
		val locs = MixxxDB.getTrackLocations()
		items.setAll(locs.map { loc ->
			var cueAmount = 0
			cues.forEach { track, cues ->
				if (track.location == loc.id)
					cueAmount = cues.size
			}
			Track(loc, cueAmount)
		})
	}
	
	fun launch() {
		App.launch {
			// todo searchRow
			val tableItems = FilteredList(items)
			val replaceRow = HBox(Label("Find"), TextField().bindText(Settings.find), Label("Replace by"), TextField().bindText(Settings.replace))
			tableItems.predicateProperty().dependOn(Settings.find) {
				val regex = Regex(it)
				return@dependOn Predicate { track -> track.loc.location.contains(regex) }
			}
			val table = TableView(tableItems)
			table.columns.addAll(TrackLocation::class.java.declaredMethods.filter { it.name.startsWith("get") }.map { method ->
				TableColumn<Track, Any?>(method.name.substring(3)) {
					method.invoke(it.value.loc)
				}
			})
			table.columns.add(TableColumn<Track, Int>("Cues") { it.value.cues })
			table.columns.removeIf { it.text in arrayOf("NeedsVerification", "Filesize") }
			table.columns.sortBy {
				when (it.text) {
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
				},
				MenuItem("Replace") {
					update(table.selectionModel.selectedItems ?: return@MenuItem) { it }
				}
			)
			table.setOnMouseClicked { me ->
				if (me.clickCount == 2)
					chooseNewLocation(table.selectedItem ?: return@setOnMouseClicked)
			}
			Scene(VBox(replaceRow, table.grow()), 1800.0, 800.0)
		}
	}
	
	fun chooseNewLocation(selected: Track) {
		val newLocation = FileChooser().apply {
			title = "Choose new location"
			initialDirectory = File(selected.loc.directory).findFolder()
		}.showOpenDialog(App.stage) ?: return
		updateLocation(selected.loc.id, newLocation)
	}
	
}

data class Track(val loc: TrackLocation, val cues: Int) {
	val location = File(loc.location)
}

private fun updateLocation(id: Long, newLocation: File) {
	MixxxDB.update("""UPDATE track_locations SET fs_deleted = 0, filename = "${newLocation.name}", directory = "${newLocation.parent}", location = "$newLocation" WHERE id LIKE $id""")
}

private fun delete(ids: Collection<Long>) {
	MixxxDB.update("DELETE FROM track_locations WHERE id IN ${ids.joinToString(",", "(", ")")}")
}

private fun delete(id: Long) {
	MixxxDB.update("DELETE FROM track_locations WHERE id = $id")
}

private fun update(list: List<Track>, update: (File) -> File) {
	list.forEach { track -> updateLocation(track.loc.id, update(track.location)) }
}