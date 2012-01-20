<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0'>

    <xsl:strip-space elements="*"/>
    
    <xsl:template match="include">
        <xsl:variable name="file" select="@ref"/>
    	<xsl:apply-templates select="document($file)/BOT-L/*"/>    	
    </xsl:template>
    
    <xsl:template match="snippet">
        <xsl:apply-templates select="./*|text()"/>
    </xsl:template>

    <xsl:template match="*">
        <xsl:copy>
            <xsl:apply-templates select="@*|*|text()"/>
        </xsl:copy>
    </xsl:template>
        
    <xsl:template match="@*|text()">
        <xsl:copy/>
    </xsl:template>

</xsl:stylesheet>