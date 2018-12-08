package xerus.music.mixxx

import xerus.ktutil.preferences.SettingsNode

object Settings: SettingsNode(getPreferences(this::class.java)) {
	val find = create("stringFind")
	val replace = create("stringReplace")
}