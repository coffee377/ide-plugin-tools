import org.jetbrains.changelog.date
import org.jetbrains.changelog.markdownToHTML
import java.util.stream.Collectors

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.7.10"
    // Gradle IntelliJ Plugin
    id("org.jetbrains.intellij") version "1.9.0"
    // Gradle Changelog Plugin
    id("org.jetbrains.changelog") version "1.3.1"
    // Gradle Qodana Plugin
    id("org.jetbrains.qodana") version "0.1.13"

    id("org.asciidoctor.jvm.convert") version "3.3.2"
    id("org.asciidoctor.jvm.pdf") version "3.3.2"
    id("org.asciidoctor.jvm.epub") version "3.3.2"

    id("io.spring.dependency-management") version "1.0.11.RELEASE"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

sourceSets["main"].java.srcDirs("src/main/gen")

repositories {
    mavenCentral()
}

dependencyManagement {
    dependencies {
        dependency("org.springframework:spring-core:4.0.3.RELEASE")
        dependency("org.junit.jupiter:junit-jupiter-api:5.8.2")
        dependency("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    }
}

dependencies {
    compileOnly("org.jsoup:jsoup:1.15.3")
    testImplementation(platform("org.junit:junit-bom:5.9.0"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}


// Set the JVM language level used to compile sources and generate files - Java 11 is required since 2020.3
kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

// Configure Gradle IntelliJ Plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    pluginName.set(properties("pluginName"))
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))

    updateSinceUntilBuild.set(false)
    downloadSources.set(false)

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version.set(properties("pluginVersion"))
    path.set("${project.projectDir}/CHANGELOG.md")
    header.set(provider { "[${version.get()}] - ${date()}" })
    itemPrefix.set("-")
    keepUnreleasedSection.set(true)
    groups.set(emptyList())
}

// Configure Gradle Qodana Plugin - read more: https://github.com/JetBrains/gradle-qodana-plugin
qodana {
    cachePath.set(projectDir.resolve(".qodana").canonicalPath)
    reportPath.set(projectDir.resolve("build/reports/inspections").canonicalPath)
    saveReport.set(true)
    showReport.set(System.getenv("QODANA_SHOW_REPORT")?.toBoolean() ?: false)
}

asciidoctorj {
    modules {
        diagram.use()
    }
//    attributesForLang(mapOf("A" to "AA", "B" to "BB"), "es")
    attributes(
        mapOf(
            "env-jetbrains" to "",
        )
    )
}

tasks {
    wrapper {
        /* gradle wrapper 版本 */
        gradleVersion = properties("gradleVersion")
    }

    test {
        useJUnitPlatform()
    }

    /* 清理项目额外删除 out 目录 */
    clean {
        delete("out")
    }

    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    /* adoc 文档生成 */
    asciidoctor {
        doFirst {
            file("build/docs").deleteRecursively()
        }

        setSourceDir(file("."))
        setOutputDir(file("build/docs"))
        sources {
            include("README*.adoc", "CHANGELOG*.adoc")
        }

//        languages("en", "es", "zh")
    }

    build {
        dependsOn(asciidoctor)
    }

    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
            freeCompilerArgs = freeCompilerArgs + "-Xjvm-default=enable"
        }
    }

    patchPluginXml {
        version.set(properties("pluginVersion"))
        pluginId.set(properties("pluginGroup"))
        sinceBuild.set(properties("pluginSinceBuild"))

        if (properties("pluginUntilBuild").isNotEmpty()) {
            untilBuild.set(properties("pluginUntilBuild"))
        }

        val descriptionFile = file("build/docs/README.html")
        if (descriptionFile.exists()) {
            val description = org.jsoup.Jsoup.parse(descriptionFile.readText(charset("UTF-8")))
                .select("#content").first()?.children()?.stream()?.map { e -> e.html() }?.collect(
                    Collectors.joining("")
                )
            pluginDescription.set(description)
        } else {
            // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
            pluginDescription.set(
                projectDir.resolve("README.md").readText().lines().run {
                    val start = "<!-- Plugin description -->"
                    val end = "<!-- Plugin description end -->"

                    if (!containsAll(listOf(start, end))) {
                        throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                    }
                    subList(indexOf(start) + 1, indexOf(end))
                }.joinToString("\n").run { markdownToHTML(this) }
            )
        }

        val changelogFile = file("build/docs/CHANGELOG.html")
        if (changelogFile.exists()) {
            val changelog = org.jsoup.Jsoup.parse(changelogFile.readText(charset("UTF-8")))
                .select("#releasenotes").first()
                ?.nextElementSibling()?.children()?.stream()?.map { e ->
                    e.html()
                        // issues id
                        .replace(
                            Regex("#([0-9]+)"),
                            "<a href=\"https://github.com/coffee377/ide-plugin-tools/issues/\$1\">#\$1</a>"
                        )
                        // regex for GitHub usernames from https://github.com/shinnn/github-username-regex
                        .replace(
                            Regex("(?i)@([a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38})"),
                            "<a href=\"https://github.com/\$1\">@\$1</a>"
                        )
                }?.collect(Collectors.joining(""))
            changeNotes.set(changelog)
        } else {
            // Get the latest available change notes from the changelog file
            changeNotes.set(provider {
                changelog.run {
                    getOrNull(properties("pluginVersion")) ?: getLatest()
                }.toHTML()
            })
        }

    }

    // Configure UI tests plugin
    // Read more: https://github.com/JetBrains/intellij-ui-test-robot
    runIdeForUiTests {
        systemProperty("robot-server.port", "8082")
        systemProperty("ide.mac.message.dialogs.as.sheets", "false")
        systemProperty("jb.privacy.policy.text", "<!--999.999-->")
        systemProperty("jb.consents.confirmation.enabled", "false")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token.set(System.getenv("PUBLISH_TOKEN"))
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        val channel = properties("pluginVersion").split("-")
            .getOrElse(1) { "default" }.split(".").first()
        channels.set(listOf(channel))
    }

    runPluginVerifier {
        ideVersions.set(listOf("2021.2.1"))
    }

    buildSearchableOptions {
        enabled.and(false)
    }

    runIde {
//        jvmArgs()
    }
}

