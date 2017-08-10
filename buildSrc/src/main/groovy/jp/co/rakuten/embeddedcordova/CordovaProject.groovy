package jp.co.rakuten.embeddedcordova

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.Project

class CordovaProject {
    Project project
    def name

    def packageName = "jp.co.rakuten.cordova.embedded"
    def cordovaDir
    def androidDir
    def config
    def buildGeneratedDir

    private def pluginsFilePath
    private def configValuesFilePath

    def cordova

    CordovaProject(Project _project, def _name) {
        this.project = _project
        this.name = _name

        buildGeneratedDir = "$project.buildDir/generated"
        cordovaDir = "$buildGeneratedDir/$name"
        androidDir = "$cordovaDir/platforms/android"
        pluginsFilePath = "$cordovaDir/plugins.txt"
        configValuesFilePath = "$cordovaDir/config.txt"
    }

    void initialize() {
        cordova = new CordovaExecutable(this.project, "$buildGeneratedDir/node_modules/.bin/cordova")
        cordova.addEnvironmentVariables(this.project.cordova.environmentVariables)

        if (this.isInitialized() && this.hasPlugins(project.cordova.cordovaPlugins) && this.hasConfigValues(project.cordova.configValues)) {
            return
        }

        this.setupProject()
        this.configureNpmRegistry(project.cordova.npmRegistry)
        this.installPlugins(project.cordova.cordovaPlugins)

        this.config = new CordovaConfig("$cordovaDir/config.xml")
        this.config.appendXml(project.cordova.configValues)
        new File(configValuesFilePath).text = JsonOutput.toJson(project.cordova.configValues)
    }

    void buildProject() {
        def gradle = new File("$androidDir/build.gradle")
        gradle.text = gradle.text.replace("apply plugin: 'com.android.application'", "apply plugin: 'com.android.library'")
        gradle.text = gradle.text.replaceAll(/applicationId[\s\S]*?\n/, "")

        cordova.build(cordovaDir, "android")

        this.addProjectDependencies()
    }

    def getDependencies() {
        def dependencies = []

        project.cordova.cordovaPlugins.each {plugin ->
            dependencies.addAll(plugin.getDependencies(cordovaDir))
        }

        return dependencies
    }

    private void setupProject() {
        this.deleteDirectory(cordovaDir)
        this.ensureFolderExists(buildGeneratedDir)

        this.npmInstall()

        cordova.create(cordovaDir, packageName, name)

        this.deleteDirectory("$cordovaDir/www")
        this.ensureFolderExists("$cordovaDir/www")

        cordova.addPlatform(cordovaDir, "android")

        this.deleteProblemFiles(androidDir)
        this.modifyAndroidManifest(androidDir)
    }

    private void npmInstall() {
        project.exec {
            def version = project.cordova.cliVersion ? project.cordova.cliVersion : 'latest'

            workingDir buildGeneratedDir
            executable 'npm'
            args 'install', "cordova@$version"
        }
    }

    private def isInitialized() {
        if (! new File(cordovaDir).exists()) {
            return false
        }

        return true
    }

    private def hasPlugins(def plugins) {
        return this.mapEqualsJsonFile(plugins, pluginsFilePath)
    }

    private def hasConfigValues(def configValues) {
        return this.mapEqualsJsonFile(configValues, configValuesFilePath)
    }

    private def mapEqualsJsonFile(def map, def filePath) {
        def jsonSlurper = new JsonSlurper()
        def file = new File(filePath)

        if (!file.exists()) {
            return false
        }

        def fileJson = jsonSlurper.parseText(file.text)
        def mapJson = jsonSlurper.parseText(JsonOutput.toJson(map))

        return mapJson == fileJson
    }

    private void addProjectDependencies() {
        project.repositories {
            flatDir{
                dirs "$androidDir/build/outputs/aar"
            }
        }

        project.dependencies {
            cordovaCompile "$packageName:android:debug@aar"
        }
    }

    private void deleteDirectory(String directory) {
        project.delete {
            delete directory
        }
    }

    private void ensureFolderExists(String directory) {
        def folder = new File(directory)

        if (!folder.exists()) {
            folder.mkdirs()
        }
    }

    private void deleteProblemFiles(String directory) {
        project.delete {
            delete project.fileTree("$directory/res") {
                include '**/drawable*/screen.png'
                include '**/mipmap*/icon.png'
            }
        }

        new File("$directory/res/values/strings.xml").text = "<?xml version='1.0' encoding='utf-8'?><resources><string name=\"app_name\">$name</string></resources>"
    }

    private void modifyAndroidManifest(String directory) {
        def manifestFile = "$directory/AndroidManifest.xml"

        new File(manifestFile).text = """
                <?xml version='1.0' encoding='utf-8'?>
                <manifest android:hardwareAccelerated="true" package="$packageName" xmlns:android="http://schemas.android.com/apk/res/android">
                    <uses-permission android:name="android.permission.INTERNET" />
                    <application>
                        <activity android:launchMode="singleTop" android:name="MainActivity" />
                    </application>
                </manifest>
                """
    }

    private void configureNpmRegistry(String url) {
        if (url) {
            cordova.addEnvironmentVariable("npm_config_registry", url)
        }
    }

    private void installPlugins(def plugins) {
        plugins.each {plugin ->
            cordova.installPlugin(cordovaDir, plugin.id, plugin.variables)
        }

        new File(pluginsFilePath).text = JsonOutput.toJson(plugins)
    }
}