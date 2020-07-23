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
	implementation(rootProject)
	implementation("org.openjfx", "javafx-controls", "11.0.1")
	implementation("ch.qos.logback", "logback-classic", "1.2.3")
	implementation("io.github.microutils", "kotlin-logging", "1.7.8")
	
}

application {
	mainClassName = "xerus.music.mixxx.MixxxFileMoverKt"
}

tasks {
	run.configure {
		args = System.getProperty("args", "").split(" ")
	}
	
	withType<KotlinCompile> {
		kotlinOptions.jvmTarget = "1.8"
	}
}
