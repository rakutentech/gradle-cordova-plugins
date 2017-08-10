package jp.co.rakuten.embeddedcordova

public class CordovaPlugin {
    final String name
    def variables = [:]
    String id

    CordovaPlugin(String _name) {
        this.name = _name
    }

    void id(String _id) {
        this.id = _id
    }

    void variable(String key, String value) {
        variables.put(key, value)
    }

    def getDependencies(def directory) {
        return this.getFrameworkDependencies(directory)
    }

    private def getFrameworkDependencies(def directory) {
        def file = new File("$directory/plugins/$id/plugin.xml")
        def config = new XmlSlurper(false, false).parseText(file.text)
        def androidPlatform = config.platform.find{it.@name == 'android'}
        def frameworks = androidPlatform.framework.findAll{it.@type != 'gradleReference'}

        return frameworks.collect{it.@src.toString()}
    }
}