plugins {
    java
    alias(libs.plugins.blossom)
}

group = "net.noxal.f3nproxyperm"
version = "1.0.1"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
}

dependencies {
    compileOnly(libs.velocity)
    compileOnly(libs.packetevents)
    compileOnly(libs.luckperms)
    annotationProcessor(libs.velocity)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

sourceSets {
    main {
        blossom {
            javaSources {
                property("version", project.version.toString())
            }
        }
    }
}