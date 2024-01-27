plugins {
    id("common-conventions")
    id("xyz.jpenilla.run-velocity")
}

tasks {
    runVelocity {
        velocityVersion("3.3.0-SNAPSHOT")

        systemProperty("terminal.jline", false)
        systemProperty("terminal.ansi", true)
        args("-p", "25520")

        downloadPlugins {
            url("https://download.luckperms.net/1526/velocity/LuckPerms-Velocity-5.4.113.jar")
            url("https://cdn.modrinth.com/data/HQyibRsN/versions/pxgKwgNJ/MiniPlaceholders-Velocity-2.2.3.jar")
        }
    }

    named("clean", Delete::class) {
        delete(project.projectDir.resolve("run"))
    }
}
