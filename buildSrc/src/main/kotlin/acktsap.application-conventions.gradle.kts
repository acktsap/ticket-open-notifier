plugins {
    // uses built-in application plugin
    // see also: https://docs.gradle.org/current/userguide/application_plugin.html
    application
}

tasks.register<Copy>("updateBin") {
    // need shadowJar result
    dependsOn(tasks["shadowJar"])

    // Specify the source directory
    from("build/libs")

    // Specify the destination directory
    into(rootProject.rootDir.resolve("bin"))

    // Optionally, include specific files or patterns
    include("*-all.jar")
}
