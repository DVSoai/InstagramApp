pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { setUrl("https://jitpack.io") }
        jcenter {
            content {
                includeModule("com.theartofdev.edmodo", "android-image-cropper")
            }
        }

    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
        jcenter {
            content {
                includeModule("com.theartofdev.edmodo", "android-image-cropper")
            }
        }

    }
}

rootProject.name = "InstagramApp"
include(":app")
 