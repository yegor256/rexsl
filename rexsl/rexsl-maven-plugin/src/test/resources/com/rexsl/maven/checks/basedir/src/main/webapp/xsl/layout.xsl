<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns="http://www.w3.org/1999/xhtml"
    xmlns:xhtml="http://www.w3.org/1999/xhtml"
    version="2.0" exclude-result-prefixes="xs xsl xhtml">
    <xsl:output method="xhtml"
        doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
        doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" />
    <xsl:template match="/">
        <html xml:lang="en">
            <head>
                <link href="/css/screen.css" rel="stylesheet" type="text/css"></link>
            </head>
            <body>
                <div id="content">
                    <xsl:call-template name="content" />
                </div>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
