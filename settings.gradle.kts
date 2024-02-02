pluginManagement {
    resolutionStrategy {
        eachPlugin {
            println("requested id=${requested.id.id}, namespace=${requested.id.namespace}, version=${requested.version}")
//            if (requested.id.id == "com.yanzhenjie.andserver.plugin") {
//                useModule("com.yanzhenjie.andserver:plugin:${requested.version}")
//            }
        }
    }
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Spider"
include(":app")
 