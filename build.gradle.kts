import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("java")
    id("de.eldoria.plugin-yml.paper") version "0.7.1"
}

version = providers.gradleProperty("project_version").get()

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.momirealms.net/releases/")
    mavenCentral()
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly(files("${rootProject.rootDir}/libs/craft-engine-core-${rootProject.properties["craftengine_version"]}.jar"))
    compileOnly(files("${rootProject.rootDir}/libs/craft-engine-bukkit-${rootProject.properties["craftengine_version"]}.jar"))
    // when block entity move into main branch I will put it back
    //compileOnly("net.momirealms:craft-engine-core:${rootProject.properties["craftengine_version"]}")
    //compileOnly("net.momirealms:craft-engine-bukkit:${rootProject.properties["craftengine_version"]}")
    compileOnly("net.momirealms:craft-engine-nms-helper:${rootProject.properties["nms_helper_version"]}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
    dependsOn(tasks.clean)
}

tasks.jar {
    destinationDirectory.set(file("$rootDir/target"))
}

paper {
    main = "com.chiiblock.plugin.ce.extension.CEExtension"
    version = rootProject.properties["project_version"] as String
    name = "LittleQMi-CE-Extension"
    apiVersion = "1.21"
    author = "iqtester"
    serverDependencies {
        register("CraftEngine") {
            required = true
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
    }
}
