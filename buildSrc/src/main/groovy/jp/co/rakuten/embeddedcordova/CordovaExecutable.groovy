import org.gradle.api.Project

class CordovaExecutable {
    Project project
    String exec
    def environmentVariables = [:]

    CordovaExecutable(Project project, String execPath) {
        this.project = project

        this.exec = execPath
    }

    void addEnvironmentVariable(String key, String value) {
        environmentVariables.put(key, value)
    }

    void addEnvironmentVariables(def variables) {
        environmentVariables << variables
    }

    void create(String directory, String packageName, String projectName) {
        project.exec {
            executable this.exec
            args "create", directory, packageName, projectName
            environment environmentVariables
        }
    }

    void addPlatform(String directory, String platform) {
        project.exec {
            workingDir directory

            executable this.exec
            args "platform", "add", platform
            environment environmentVariables
        }
    }

    void installPlugin(String directory, String id, def variables) {
        project.exec {
            def pluginArgs = ["plugin", "add", id]

            variables.each{key, value ->
                pluginArgs.add("--variable")
                pluginArgs.add("$key=$value")
            }

            workingDir directory
            executable this.exec
            args = pluginArgs
            environment environmentVariables
        }
    }

    void build(String directory, String platform) {
        project.exec {
            workingDir directory
            executable this.exec
            args "build", platform
            environment environmentVariables
        }
    }
}