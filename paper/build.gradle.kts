plugins {
    id("paper-conventions")
}

dependencies {
    api(projects.parrotidCore)
    compileOnly(libs.paper.api)
}

applyJarMetadata("parrot-paper")
