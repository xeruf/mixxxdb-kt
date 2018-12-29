package xerus.music.mixxx

import xerus.ktutil.preferences.SettingsNode

object Settings: SettingsNode(getPreferences(this::class)) {
	val FIND = create("stringFind")
	val REPLACE = create("stringReplace")
	val CASESENSITIVE = create("stringCaseSensitive", true)
}