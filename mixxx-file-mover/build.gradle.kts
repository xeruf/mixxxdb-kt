import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm")
	application
}

sourceSets {
	getByName("main") {
		java.srcDir("src/main")
		resources.srcDir("src/resources")
	}
	getByName("test") {
		java.srcDir("src/test")
	}
}

dependencies {
	compile(rootProject)
	compile("org.openjfx", "javafx-controls", "11.0.1")
	compile("ch.qos.logback", "logback-classic", "1.2.3")
	compile("io.github.microutils", "kotlin-logging", "1.6.22")
	
}

application {
	mainClassName = "xerus.music.mixxx.MixxxFileMoverKt"
}

tasks.withType<KotlinCompile> {
	kotlinOptions.jvmTarget = "1.8"
}
