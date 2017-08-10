package jp.co.rakuten.embeddedcordova

import org.gradle.api.Plugin
import org.gradle.api.Project

class EmbeddedCordova implements Plugin<Project> {
    Project mProject

    void apply(Project project) {
        mProject = project

        mProject.extensions.create('cordova', EmbeddedCordovaExtension)
        def plugins = mProject.container(CordovaPlugin)
        mProject.cordova.extensions.add("cordovaPlugins", plugins)

        mProject.configurations {
            cordovaCompile
            compile.extendsFrom(cordovaCompile)
        }

        mProject.dependencies.add("cordovaCompile", "org.apache.cordova:framework:6.1.2:release@aar")

        def cordovaProject = new CordovaProject(project, 'cordova-project')

        mProject.gradle.afterProject { evaluatedProject ->
            if (evaluatedProject != mProject) {
                return
            }

            cordovaProject.initialize()

            this.applyPluginGradleFiles(cordovaProject.androidDir, mProject.cordova.cordovaPlugins)
            this.addDependencies(cordovaProject.getDependencies())

            cordovaProject.buildProject()
        }
    }

    private void applyPluginGradleFiles(String directory, def pluginList) {
        pluginList.each{plugin ->
            def gradleFiles = new FileNameFinder().getFileNames(directory, "$plugin.id/*.gradle")

            gradleFiles.each {file ->
                def gradleFile = new File(file)
                def gradleContent = gradleFile.text

                gradleContent = gradleContent.replaceAll(/(dirs[\s]*?)(\(?\'|\")([^\\/].*?)(\'|\"\)?)/) {all, dirs, quoteBegin, source, quoteEnd ->
                    "$dirs$quoteBegin$directory/$source$quoteEnd"
                }

                gradleFile.text = gradleContent
                mProject.apply(from: file)
            }
        }
    }

    private void addDependencies(def dependencies) {
        dependencies.each{dependency ->
            this.addDependency('cordovaCompile', dependency)
        }
    }

    private void addDependency(String dependency) {
        project.dependencies.add('cordovaCompile', dependency)
    }
}
