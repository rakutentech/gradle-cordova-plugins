package jp.co.rakuten.embeddedcordova

class EmbeddedCordovaExtension {
    String npmRegistry
    String cliVersion
    def configValues = []
    def environmentVariables = [:]

    void npmRegistry(String registry) {
        this.npmRegistry = registry
    }

    void cliVersion(String version) {
        this.cliVersion = version
    }

    void configValue(String value) {
        configValues.add(value)
    }

    void environmentVariable(String key, String value) {
        environmentVariables.put(key, value)
    }
}