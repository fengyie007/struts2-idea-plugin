import org.jetbrains.intellij.platform.gradle.TestFrameworkType

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

sourceSets {
    main {
        java {
            srcDir("src/main/gen")
        }
    }
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
        testFramework(TestFrameworkType.Plugin.Java)
    }

    // 测试依赖 - 支持JUnit 4和JUnit 5
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.9.23")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.10.2")

    // IntelliJ Platform 测试框架 - 提供额外的测试API
    // 这个依赖提供了某些测试用例需要的特殊API，如 addSuppressedException 等
    testImplementation("com.jetbrains.intellij.platform:test-framework:251.26927.53")
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

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
        options.encoding = "UTF-8"
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    buildSearchableOptions {
        enabled = false
    }
}