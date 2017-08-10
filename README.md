# Cordova Embedded Webview Gradle Plugin

This repository is a proof of concept that demonstrates a Gradle plugin for integrating Cordova WebViews into an existing Native app. It also allows Cordova plugins to be easily used within the WebView. It consists of three parts:

## Gradle Plugin
- The Gradle plugin will generate a Cordova project as a build artifact and add the correct sources to your App project for both Cordova and any specified Cordova plugins
- Cordova plugins and other Cordova options can be configured in build.gradle

## Android Library:
- Provides a Fragment with Cordova WebView functionality that can be dropped into any View
- Can also specify what url to load for each fragment

## Sample Application
- A simple app that makes use of the Gradle plugin and WebView library

# Usage

This repository already includes a sample app, but the steps below describe how the Gradle plugin and Cordova WebView could be used in another project.

## 1. Apply the gradle plugin in App's `build.gradle`

```groovy
apply plugin: 'jp.co.rakuten.embeddedcordova'
```

## 2. Set configuration and add Cordova plugins

The following configuration can only be added after the plugin has already been applied.

```groovy
cordova {
    cordovaPlugins {
        nameOfPlugin {
            id 'plugin-id'
            variable 'variable_1_key', 'variable_value'
            variable 'variable_2_key', 'variable_value'
        }
        anotherPlugin {
            id 'http://www.example.com/gitrepo'
        }
    }
}
```

**Additional Configuration DSL**
`environmentVariable 'key', 'value'` - an environment variable that will be applied when running Cordova CLI commands
`configValue '<xmlString />'` - an XML string that will be appended to the Cordova project's config.xml file
`cliVersion '7.0.1'` - version of cordova-cli which will be used to build the Cordova project
`npmRegistry 'url'` - url of npm registry where plugins will be fetched from

## 3. Create Cordova Web Assets

Web assets for Cordova should be in the `assets/www` folder for your build variant. These web assets will work identically to a standard Cordova project, so the web project setup should be the same as a Cordova project This means that HTML files should include a `<script>` tag for `cordova.js`.

## 4. Add the EmbeddedCordovaWebview

You can choose to specify the starting URL by using the attribute `app:cordova_url` (relative to `assets/www` folder).

```xml
    <fragment android:name="jp.co.rakuten.embeddedcordova.EmbeddedCordovaFragment"
              app:cordova_url="index2.html"
              android:layout_width="match_parent"
              android:layout_height="0dp"
              android:layout_weight=".5"
              android:layout_marginTop="@dimen/activity_vertical_margin"/>
```

## Advanced Usage

### Managing Dependencies

Plugin dependencies can be overridden using a ResolutionStrategy.

```groovy
configurations.compile.resolutionStrategy {
    force 'package.library:3.2.0'
}
```

# Gradle Plugin Process

The following is a generic outline of the process used by the Gradle plugin:

1. Creates a Cordova project in the App’s build directory
2. Add android platform to the project
3. Modify default Cordova project to avoid conflicts with the main App
    a. Remove things like launch icons and the splash screen
    b. AndroidManifest is modified so that it is as basic as possible
    c. Delete default content in ‘www’ folder
4. Install Cordova plugins using provided plugin variables
5. Search for Gradle files from the installed Cordova Plugins and apply them to the App Project
    a. It would be better if instead these Gradle files were parsed for dependencies and repositories and only these applied. Some plugins do weird things in the Gradle file like changing the ApplicationId. Also this step is the main reason that the plugin must currently run on the Gradle “configuration” step rather than the “tasks” step - because the Gradle files cannot be applied after “tasks” has already started.
6. Find additional dependencies declared by plugins (<framework> tags in plugin.xml) and add them to the App Project
7, Build the Cordova project as a library and add the library dependency to the App Project
