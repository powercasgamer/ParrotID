@file:Suppress("ktlint")

import net.kyori.indra.git.IndraGitExtension
import org.eclipse.jgit.lib.Repository
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.*
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import java.text.SimpleDateFormat
import java.util.*

import com.diffplug.gradle.spotless.BaseKotlinExtension
import com.diffplug.gradle.spotless.FormatExtension
import com.diffplug.gradle.spotless.SpotlessExtension

val Project.libs: LibrariesForLibs
    get() = the()

// set by GitHub Actions
val Project.ci: Provider<Boolean>
    get() = providers.environmentVariable("CI")
        .map { it.toBoolean() }
        .orElse(false)

fun Project.lastCommitHash(error: Boolean = false): String =
    the<IndraGitExtension>().commit()?.name?.substring(0, 7)
        ?: if (error) error("Could not determine commit hash") else "unknown"

fun Project.currentBranch(): String {
    System.getenv("GITHUB_HEAD_REF")?.takeIf { it.isNotEmpty() }
        ?.let { return it }
    System.getenv("GITHUB_REF")?.takeIf { it.isNotEmpty() }
        ?.let { return it.replaceFirst("refs/heads/", "") }

    val indraGit = the<IndraGitExtension>().takeIf { it.isPresent }

    val ref = indraGit?.git()?.repository?.exactRef("HEAD")?.target
        ?: return "detached-head"

    return Repository.shortenRefName(ref.name)
}

fun Project.versionString(): String = this.version as String

fun Project.nameString(useProjectName: Boolean = false): String =
    if (useProjectName) {
        val projName = providers.gradleProperty("projectName").getOrElse("template")
        if (this.name.contains('-')) {
            val splat = this.name.split("-").toMutableList()
            splat[0] = projName
            splat.joinToString("-") { it.uppercaseFirstChar() }
        } else {
            projName
        }
    } else if (this.name.contains('-')) {
        this.name.split("-").joinToString("-") { it.uppercaseFirstChar() }
    } else {
        this.name.uppercaseFirstChar()
    }

fun BaseKotlinExtension.KtlintConfig.overrides() {
    editorConfigOverride(
        mapOf(
            "ktlint_standard_filename" to "disabled",
            "ktlint_standard_trailing-comma-on-call-site" to "disabled",
            "ktlint_standard_trailing-comma-on-declaration-site" to "disabled",
        )
    )
}

fun Project.applyJarMetadata(moduleName: String) {
    if ("jar" in tasks.names) {
        tasks.named<Jar>("jar") {
            manifest.attributes(
                "Multi-Release" to "true",
                "Built-By" to System.getProperty("user.name"),
                "Created-By" to System.getProperty("java.vendor.version"),
                "Build-Jdk" to System.getProperty("java.version"),
                "Build-Time" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm.sssZ").format(Date()),
                "Automatic-Module-Name" to moduleName,
                "Specification-Title" to moduleName,
                "Specification-Version" to project.versionString(),
                "Specification-Vendor" to providers.gradleProperty("projectAuthor").getOrElse("athena"),
            )
            val indraGit = project.extensions.findByType<IndraGitExtension>()
            indraGit?.apply {
                applyVcsInformationToManifest(manifest)
            }
        }
    }
}

fun ExternalModuleDependency.excludeUseless() {
    exclude(group = "org.jetbrains", module = "annotations")
    exclude(group = "org.checkerframework", module = "checker-qual")
    exclude(group = "com.google.errorprone", module = "error_prone_core")
    exclude(group = "com.google.errorprone", module = "error_prone_annotations")
    exclude(group = "com.google.errorprone", module = "error_prone_annotation")
    exclude(group = "com.google.code.findbugs", module = "jsr305")
    exclude(group = "com.google.j2objc", module = "j2objc-annotations")
}

fun Configuration.excludeUseless() {
    exclude(group = "org.jetbrains", module = "annotations")
    exclude(group = "org.checkerframework", module = "checker-qual")
    exclude(group = "com.google.errorprone", module = "error_prone_core")
    exclude(group = "com.google.errorprone", module = "error_prone_annotations")
    exclude(group = "com.google.errorprone", module = "error_prone_annotation")
    exclude(group = "com.google.code.findbugs", module = "jsr305")
    exclude(group = "com.google.j2objc", module = "j2objc-annotations")
}

/**
 * Returns `true` if a substring of this char sequence starting at the specified offset [startIndex] starts with any of the specified prefixes.
 */
fun CharSequence.startsWithAny(vararg prefixes: CharSequence, startIndex: Int = 0, ignoreCase: Boolean = false): Boolean {
    return prefixes.any { this.startsWith(it, startIndex, ignoreCase) }
}

/**
 * Returns `true` if this char sequence ends with any of the specified suffixes.
 */
fun CharSequence.endsWithAny(vararg suffixes: CharSequence, ignoreCase: Boolean = false): Boolean {
    return suffixes.any { this.endsWith(it, ignoreCase) }
}