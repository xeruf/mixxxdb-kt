import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.3.11"
	`java-library`
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
	api("com.github.Xerus2000.util", "javafx", "master-SNAPSHOT")
	
	compile("org.xerial", "sqlite-jdbc", "3.25.2")
}
