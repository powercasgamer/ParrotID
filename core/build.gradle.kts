plugins {
    id("common-conventions")
    id("checkstyle")
}

logger.lifecycle(
    """
*******************************************
 You are building ParrotID!

 If you encounter trouble:
 1) Read README.md if you haven't yet
 2) Try running 'build' in a separate Gradle run
 3) Use gradlew and not gradle
 4) If you still need help, don't.

 Output files will be in build/libs
*******************************************
"""
)

repositories {
    mavenCentral()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://maven.enginehub.org/repo/") }
}

configurations.all {
    resolutionStrategy {
        force(libs.guava)
    }
}

dependencies {
    compileOnlyApi(libs.jspecify)
    compileOnly("org.xerial:sqlite-jdbc:3.45.0.0")
    compileOnly(libs.paper.api)
    compileOnlyApi("org.xerial:sqlite-jdbc:3.36.0.3")
    compileOnlyApi(libs.mongodb)
    testImplementation(libs.guava)
    compileOnlyApi(libs.guava)
    compileOnlyApi(libs.gson)
    testImplementation(libs.gson)
    "testImplementation"("org.xerial:sqlite-jdbc:3.36.0.3")
//    "testImplementation"("junit:junit:${Versions.JUNIT}")
//    "testImplementation"("org.junit.jupiter:junit-jupiter-api:${Versions.JUPITER}")
//    "testImplementation"("org.junit.jupiter:junit-jupiter-params:${Versions.JUPITER}")
    "testImplementation"("org.hamcrest:hamcrest:2.2")
//    "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:${Versions.JUPITER}")
}
