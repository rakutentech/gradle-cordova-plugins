import groovy.xml.XmlUtil

class CordovaConfig {
    def file
    def xml

    CordovaConfig(String path) {
        file = new File(path)
        xml = new XmlSlurper(false, false).parseText(file.text)
    }

    void appendXml(String xmlString) {
        this.appendTextToXml(xmlString)

        file.text = XmlUtil.serialize(xml)
    }

    void appendXml(List<String> xmlStrings) {
        xmlStrings.each {xml ->
            this.appendTextToXml(xml)
        }

        file.text = XmlUtil.serialize(xml)
    }

    private def appendTextToXml(String text) {
        def node = new XmlSlurper(false, false).parseText(text)

        if (node) {
            xml.appendNode(node)
        }
    }
}