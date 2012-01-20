<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0'>
	
	<xsl:strip-space elements="*"/>
	
	<xsl:template match="/">
		<xsl:apply-templates select="*"/>
	</xsl:template>
        
	<xsl:template match="use">
		<xsl:variable name="function-name" select="@name"/>
		<xsl:apply-templates select="//declare/function[@name=$function-name]/node()"/>
		<xsl:text>;</xsl:text>
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