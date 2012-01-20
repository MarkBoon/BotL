<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

<xsl:output method="xml"/>

<xsl:strip-space elements="*"/>	
	
<xsl:template match="/">
	<xsl:apply-templates select="*"/>
</xsl:template>

<xsl:template match="*">
	<xsl:copy>
		<xsl:for-each select="@*">
			<xsl:copy/>
		</xsl:for-each>
		<xsl:apply-templates select="*"/>
		<xsl:variable name="regexp">
	  		<xsl:text>#([a-zA-Z][a-zA-Z0-9-]*)</xsl:text>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="matches(text()[1],$regexp)">
				<xsl:variable name="matched-string">
					<xsl:call-template name="get-variable">
						<xsl:with-param name="input" select="text()"/>
						<xsl:with-param name="pattern" select="$regexp"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:value-of select="$matched-string"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select="text()"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:copy>
</xsl:template>

<xsl:template name="get-variable">
	<xsl:param name="input"/>
	<xsl:param name="pattern"/>
	
	<xsl:choose>
		<xsl:when test="matches($input,$pattern)">
 			<xsl:variable name="replacement" select="replace($input,$pattern,'local:get(&quot;&#36;1&quot;)')"/>
			<xsl:value-of select="$replacement"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select="$input"/>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

</xsl:transform>