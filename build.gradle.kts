plugins {
    `java-library`
    id("com.vanniktech.maven.publish") version "0.37.0"
}

group = "de.pauleff"
version = "2.0.0-SNAPSHOT"
description = "A modern, efficient Java library for reading, editing, and writing Minecraft NBT files"

base {
    archivesName.set("jnbt")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(26)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(26)
    options.compilerArgs.add("-parameters")
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.test {
    useJUnitPlatform()
}

tasks.javadoc {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).apply {
        addBooleanOption("html5", true)
        addBooleanOption("notimestamp", true)
        windowTitle("jNBT ${project.version} API")
        docTitle("jNBT ${project.version}")
    }
}

tasks.jar {
    manifest {
        attributes(
            mapOf(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version.toString(),
                "Specification-Title" to project.name,
                "Specification-Version" to project.version.toString(),
            ),
        )
    }
}

tasks.assemble {
    dependsOn("plainJavadocJar")
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = false)
    signAllPublications()

    coordinates(group.toString(), "jnbt", version.toString())

    pom {
        name.set("jNBT")
        description.set(project.description)
        url.set("https://github.com/paul-eff/jNBT")
        inceptionYear.set("2022")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("paul-eff")
                name.set("paul-eff")
                email.set("noreply@paul-eff.github.io")
                url.set("https://github.com/paul-eff")
                timezone.set("Europe/Berlin")
            }
        }

        scm {
            connection.set("scm:git:https://github.com/paul-eff/jNBT.git")
            developerConnection.set("scm:git:git@github.com:paul-eff/jNBT.git")
            url.set("https://github.com/paul-eff/jNBT")
        }

        issueManagement {
            system.set("GitHub Issues")
            url.set("https://github.com/paul-eff/jNBT/issues")
        }
    }
}
