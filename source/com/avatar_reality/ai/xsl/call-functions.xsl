<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0'>
    
    <xsl:output method="xml"/>
    
    <xsl:strip-space elements="*"/>
    
    <xsl:template match="/">
        <xsl:apply-templates select="*"/>
    </xsl:template>
    
    <xsl:template match="call">
        <xsl:variable name="function-name" select="@name"/>
        <xsl:variable name="function-args" select="@args"/>
        <xsl:element name="execute">
            <xsl:element name="use">
                <xsl:attribute name="name" select="$function-name"/>
            </xsl:element>
            <xsl:value-of select="$function-name"/>
            <xsl:value-of select="$function-args"/>
        </xsl:element>
    </xsl:template>   	
    <xsl:template match="set">
        <xsl:element name="execute">
            <xsl:text>local:set('</xsl:text>
            <xsl:value-of select="@name"/>
            <xsl:text>',</xsl:text>
            <xsl:value-of select="@value"/>
            <xsl:text>)</xsl:text>
        </xsl:element>
    </xsl:template>   	
    
    <xsl:template match="*">
        <xsl:copy>
            <xsl:for-each select="@*">
                <xsl:copy/>
            </xsl:for-each>
            <xsl:apply-templates select="*"/>
            <xsl:apply-templates select="text()"/>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>