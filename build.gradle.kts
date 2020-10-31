import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.4.10"
	`java-library`
	
	id("com.github.ben-manes.versions") version "0.33.0"
	id("se.patrikerdes.use-latest-versions") version "0.2.15"
}

allprojects {
	repositories {
		jcenter()
		maven("https://jitpack.io")
	}
	
	tasks.withType<KotlinCompile> {
		kotlinOptions.jvmTarget = "1.8"
	}
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
	api(kotlin("stdlib"))
	api("com.github.Xerus2000.util", "javafx", "2f67fc2")
	
	implementation("org.xerial", "sqlite-jdbc", "3.32.3.2")
}
