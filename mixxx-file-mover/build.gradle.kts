import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm")
	application
}

sourceSets {
	getByName("main") {
		java.srcDir("src/main")
	}
	getByName("test") {
		java.srcDir("src/test")
	}
}

dependencies {
	compile(rootProject)
}

application {
	mainClassName = "xerus.music.mixxx.MixxxFileMoverKt"
}

tasks.withType<KotlinCompile> {
	kotlinOptions.jvmTarget = "1.8"
}
