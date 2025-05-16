pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {//1行に書いてしまうとだめっぽい。こうやって書かないと。https://stackoverflow.com/a/76888815/15819684
            url = uri("https://jitpack.io")
        }
    }
}

rootProject.name = "HouseholdExpenses"
include(":app")