import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.3.61"
	`java-library`
	id("com.github.ben-manes.versions") version "0.27.0"
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
	api(kotlin("stdlib-jdk8"))
	api("com.github.Xerus2000.util", "javafx", "a0831c8")
	
	implementation("org.xerial", "sqlite-jdbc", "3.30.1")
}
