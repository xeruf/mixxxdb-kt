package xerus.music.mixxx

import javafx.collections.FXCollections
import javafx.scene.Scene
import javafx.scene.control.TableView
import javafx.scene.layout.VBox
import xerus.ktutil.javafx.TableColumn
import xerus.ktutil.javafx.grow
import xerus.ktutil.javafx.ui.App

fun main(args: Array<String>) {
	MixxxDB.connect()
	App.launch {
		val items = FXCollections.observableArrayList(MixxxDB.getLibrary())
		val table = TableView(items)
		table.columns.addAll(Song::class.java.declaredMethods.filter { it.name.startsWith("get") }.map { method ->
			TableColumn<Song, Any?>(method.name.substring(3)) {
				method.invoke(it.value)
			}
		})
		table.columns.sortBy {
			when (it.text) {
				"Id" -> 0
				"Artist" -> 1
				"Title" -> 2
				"Album" -> 3
				"Tracknumber" -> 4
				else -> 9999
			}
		}
		Scene(VBox(table.grow()))
	}
}