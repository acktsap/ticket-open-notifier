import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    // to make fat-jar
    // see also: https://gradleup.com/shadow/introduction/
    id("com.gradleup.shadow")
}

tasks.named<ShadowJar>("shadowJar") {
    // custom names
    archiveBaseName.set(project.rootProject.name)
    archiveClassifier.set("all")
    // archiveVersion.set("") // if want to customize version

    // remove all classes of dependencies that are not used by the project
    minimize()

    // show logging
    doLast {
        println("Shadow jar is generated in '${project.layout.buildDirectory.get()}/libs'")
    }
}
