<?xml version="1.0"?>
<!--
 * Copyright (c) 2011-2014, ReXSL.com
 * All rights reserved.
 -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns="http://www.w3.org/1999/xhtml" version="2.0" exclude-result-prefixes="xs">
    <xsl:template match="/">
        <xsl:text disable-output-escaping="yes">&lt;!DOCTYPE html&gt;</xsl:text>
        <xsl:apply-templates select="page"/>
    </xsl:template>
    <xsl:template match="page">
        <html lang="en">
            <head>
                <meta charset="UTF-8"/>
                <meta name="description" content="ReXSL-powered web site, demo version"/>
                <meta name="keywords" content="Java, XSL, JAXB, JAX-RS, ReXSL"/>
                <meta name="author" content="${groupId} ${artifactId}"/>
                <link rel="stylesheet" type="text/css" media="all">
                    <xsl:attribute name="href">
                        <xsl:text>/css/screen.css?</xsl:text>
                        <xsl:value-of select="/page/version/revision"/>
                    </xsl:attribute>
                </link>
                <link rel="icon" type="image/gif">
                    <xsl:attribute name="href">
                        <xsl:text>http://img.rexsl.com/favicon.gif</xsl:text>
                        <xsl:value-of select="/page/version/revision"/>
                    </xsl:attribute>
                </link>
                <xsl:call-template name="head"/>
            </head>
            <body>
                <xsl:apply-templates select="version"/>
                <div id="content">
                    <p>
                        <a>
                            <xsl:attribute name="href">
                                <xsl:value-of select="/page/links/link[@rel='home']/@href"/>
                            </xsl:attribute>
                            <img alt="logo">
                                <xsl:attribute name="src">
                                    <xsl:text>http://img.rexsl.com/logo.png?</xsl:text>
                                    <xsl:value-of select="/page/version/revision"/>
                                </xsl:attribute>
                            </img>
                        </a>
                    </p>
                    <xsl:call-template name="content"/>
                </div>
            </body>
        </html>
    </xsl:template>
    <xsl:template name="millis">
        <xsl:param name="millis" as="xs:integer"/>
        <xsl:choose>
            <xsl:when test="$millis &gt; 1000">
                <xsl:value-of select="format-number($millis div 1000, '0.0')"/>
                <xsl:text>s</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="format-number($millis, '#')"/>
                <xsl:text>ms</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="version">
        <div id="version">
            <xsl:value-of select="name"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="revision"/>
            <xsl:text> </xsl:text>
            <xsl:call-template name="millis">
                <xsl:with-param name="millis" select="/page/millis"/>
            </xsl:call-template>
        </div>
    </xsl:template>
</xsl:stylesheet>
