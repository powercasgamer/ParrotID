import java.io.File

plugins {
    id("common-conventions")
    application
}

tasks {
    runShadow {
        systemProperty("terminal.jline", false)
        systemProperty("terminal.ansi", true)

        workingDir = file("run").also(File::mkdirs)
    }
}
