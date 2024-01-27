plugins {
    id("common-conventions")
    id("checkstyle")
}


logger.lifecycle("""
*******************************************
 You are building SquirrelID!

 If you encounter trouble:
 1) Read README.md if you haven't yet
 2) Try running 'build' in a separate Gradle run
 3) Use gradlew and not gradle
 4) If you still need help, ask on Discord! https://discord.gg/enginehub

 Output files will be in build/libs
*******************************************
""")

repositories {
    mavenCentral()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://maven.enginehub.org/repo/") }
}

configurations.all {
    resolutionStrategy {
//        force("com.google.guava:guava:${Versions.GUAVA}")
    }
}

dependencies {
//    "implementation"("com.google.guava:guava:${Versions.GUAVA}")
    compileOnlyApi(libs.jspecify)
    compileOnly("org.xerial:sqlite-jdbc:3.36.0.3")
    api("org.mongodb:mongodb-driver-sync:4.11.1")
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")

    "testImplementation"("com.google.guava:guava:33.0.0-jre")
    "testImplementation"("com.google.code.findbugs:jsr305:1.3.9")
    "testImplementation"("org.xerial:sqlite-jdbc:3.36.0.3")
    "testImplementation"("com.googlecode.json-simple:json-simple:1.1.1")
//    "testImplementation"("junit:junit:${Versions.JUNIT}")
//    "testImplementation"("org.junit.jupiter:junit-jupiter-api:${Versions.JUPITER}")
//    "testImplementation"("org.junit.jupiter:junit-jupiter-params:${Versions.JUPITER}")
    "testImplementation"("org.hamcrest:hamcrest:2.2")
//    "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:${Versions.JUPITER}")
}