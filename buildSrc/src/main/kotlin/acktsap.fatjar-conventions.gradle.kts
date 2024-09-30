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

    // show logging
    doLast {
        println("Shadow jar is generated in '${project.layout.buildDirectory.get()}/libs'")
    }
}
