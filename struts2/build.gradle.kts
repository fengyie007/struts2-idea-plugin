plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.7.0"
    id("org.jetbrains.kotlin.jvm") version "1.9.23"
}

group = "com.intellij"
version = "2025.1.3"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

java.sourceSets["main"].java {
    srcDir("src/main/gen")
}

dependencies {
    intellijPlatform {
        intellijIdeaUltimate("2025.1.3")

        bundledPlugins(
            "com.intellij.javaee",
            "com.intellij.javaee.web",
            "com.intellij.spring",
            "com.intellij.freemarker",
            "com.intellij.velocity",
            "org.intellij.groovy",
            "com.intellij.jsp",
            "JavaScript",
            "com.intellij.java-i18n"
        )

        pluginVerifier()
        zipSigner()
        instrumentationTools()
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "241"
            untilBuild = "252.*"
        }
    }

    signing {
        certificateChain = System.getenv("CERTIFICATE_CHAIN")
        privateKey = System.getenv("PRIVATE_KEY")
        password = System.getenv("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = System.getenv("PUBLISH_TOKEN")
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
}

tasks.buildSearchableOptions {
    enabled = false
}