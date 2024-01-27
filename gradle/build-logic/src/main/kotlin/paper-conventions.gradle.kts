plugins {
    id("common-conventions")
    id("xyz.jpenilla.run-paper")
}

tasks {
    runServer {
        minecraftVersion("1.20.4")

        jvmArguments.add("-Dcom.mojang.eula.agree=true")
        systemProperty("terminal.jline", false)
        systemProperty("terminal.ansi", true)
        args("-p", "25519")

        downloadPlugins {
            url("https://cdn.modrinth.com/data/HQyibRsN/versions/M6gjRuIx/MiniPlaceholders-Paper-2.2.3.jar")
            url("https://download.luckperms.net/1526/bukkit/loader/LuckPerms-Bukkit-5.4.113.jar")
        }
    }

    named("clean", Delete::class) {
        delete(project.projectDir.resolve("run"))
    }
}