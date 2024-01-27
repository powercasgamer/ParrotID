import com.diffplug.gradle.spotless.FormatExtension
import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer
import net.kyori.indra.licenser.spotless.HeaderFormat
import java.util.Calendar
import java.util.Date

plugins {
    id("base-conventions")
    id("net.kyori.indra")
    id("net.kyori.indra.publishing")
    id("net.kyori.indra.git")
    id("net.kyori.indra.licenser.spotless")
    id("com.github.johnrengelman.shadow")
    id("java-library")
}

val libs = extensions.getByType(org.gradle.accessors.dm.LibrariesForLibs::class)

extensions.getByType(BasePluginExtension::class.java).archivesName.set(project.nameString(true))

indra {
    javaVersions {
        minimumToolchain(21)
        target(17)
    }
    mitLicense()

    publishSnapshotsTo("mizule", "https://repo.mizule.dev/snapshots")
    publishReleasesTo("mizule", "https://repo.mizule.dev/releases")
}

java {
    withSourcesJar()
    withJavadocJar()
}

spotless {
    fun FormatExtension.applyCommon(spaces: Int = 4) {
        trimTrailingWhitespace()
        indentWithSpaces(spaces)
        endWithNewline()
        encoding("UTF-8")
        toggleOffOn()
    }
    java {
        importOrderFile(rootProject.file(".spotless/mizule.importorder"))
        removeUnusedImports()
        formatAnnotations()
        applyCommon()
    }
    kotlinGradle {
        applyCommon()
        ktlint("0.50.0").overrides()
//        ktlint(libs.versions.ktlint.get())
    }
    kotlin {
        applyCommon()
        ktlint("0.50.0").overrides()
//        ktlint(libs.versions.ktlint.get())
    }
    format("configs") {
        target("**/*.yml", "**/*.yaml", "**/*.json", "**/*.conf")
        targetExclude("run/**")
        applyCommon(2)
    }
}

indraSpotlessLicenser {
    headerFormat(HeaderFormat.starSlash())
    licenseHeaderFile(rootProject.projectDir.resolve("HEADER"))

    val currentYear =
        Calendar.getInstance().apply {
            time = Date()
        }.get(Calendar.YEAR)
    val createdYear = providers.gradleProperty("createdYear").map { it.toInt() }.getOrElse(currentYear)
    val year = if (createdYear == currentYear) createdYear.toString() else "$createdYear-$currentYear"

    property("name", providers.gradleProperty("projectName").getOrElse("template"))
    property("year", year)
    property("description", project.description ?: "A template project")
    property("author", providers.gradleProperty("projectAuthor").getOrElse("template"))
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }

    jar {
        archiveClassifier.set("unshaded")
        from(rootProject.projectDir.resolve("LICENSE")) {
            rename { "LICENSE_${providers.gradleProperty("projectName").getOrElse("template")}" }
        }
    }

    shadowJar {
        archiveClassifier.set("")
        mergeServiceFiles()
        transform(Log4j2PluginsCacheFileTransformer::class.java)
    }

    clean {
        delete("run")
    }

    create("format") {
        dependsOn("spotlessApply")
    }

    spotlessCheck {
        dependsOn(gradle.includedBuild("build-logic").task(":spotlessCheck"))
    }
    spotlessApply {
        dependsOn(gradle.includedBuild("build-logic").task(":spotlessApply"))
    }

    withType<JavaCompile>().configureEach {
        options.isFork = true
        options.isIncremental = true
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
        options.compilerArgs.add("-Xlint:-processing")
    }

    withType<ProcessResources>().configureEach {
        filteringCharset = "UTF-8"
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        val props =
            mapOf(
                "pluginVersion" to project.versionString(),
                "pluginAuthor" to providers.gradleProperty("projectAuthor").getOrElse("template"),
                "pluginName" to providers.gradleProperty("projectName").getOrElse("template"),
                "pluginDescription" to (project.description ?: "A template project"),
            )

        filesMatching(setOf("paper-plugin.yml", "plugin.yml", "velocity-plugin.json")) {
            expand(props)
        }
    }

    javadoc {
        val options = options as? StandardJavadocDocletOptions ?: return@javadoc
        options.isAuthor = true
        options.encoding = "UTF-8"
        options.charSet = "UTF-8"
        options.addStringOption("Xdoclint:none", "-quiet")
        options.addBooleanOption("Xdoclint:none", true)
        options.linkSource(true)
        options.tags(
            "apiNote:a:API Note:",
            "implSpec:a:Implementation Requirements:",
            "implNote:a:Implementation Note:"
        )
    }
}
