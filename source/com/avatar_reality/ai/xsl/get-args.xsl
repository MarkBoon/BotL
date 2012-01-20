<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

<xsl:strip-space elements="*"/>
	
<xsl:output method="xml"/>

<xsl:template match="/">
	<xsl:apply-templates select="*"/>
</xsl:template>

<xsl:template match="*">
	<xsl:copy>
		<xsl:for-each select="@*">
			<xsl:copy/>
		</xsl:for-each>
		<xsl:apply-templates select="*"/>
		<xsl:variable name="regexpSingle">
	  		<xsl:text>(&apos;([^&apos;\\]|\\.)*\$\d+([^&apos;\\]|\\.)*&apos;)</xsl:text>
		</xsl:variable>
		<xsl:variable name="regexpDouble">
			<xsl:text>(&quot;([^&quot;\\]|\\.)*\$\d+([^&quot;\\]|\\.)*&quot;)</xsl:text>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="matches(text()[1],'fn:replaces')">
				<xsl:apply-templates select="text()"/>
			</xsl:when>
			<xsl:when test="matches(text()[1],$regexpSingle)">
				<xsl:variable name="quoted-string">
					<xsl:call-template name="get-quoted-string">
						<xsl:with-param name="input" select="text()"/>
						<xsl:with-param name="pattern" select="$regexpSingle"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:value-of select="$quoted-string"/>
			</xsl:when>
			<xsl:when test="matches(text()[1],$regexpDouble)">
				<xsl:variable name="quoted-string">
					<xsl:call-template name="get-quoted-string">
						<xsl:with-param name="input" select="text()"/>
						<xsl:with-param name="pattern" select="$regexpDouble"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:value-of select="$quoted-string"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select="text()"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:copy>
</xsl:template>

<xsl:template name="get-quoted-string">
	<xsl:param name="input"/>
	<xsl:param name="pattern"/>
	
	<xsl:choose>
		<xsl:when test="matches($input,$pattern)">
			<xsl:variable name="replacement" select="replace($input,$pattern,'local:args(&#36;1)')"/>
			<xsl:value-of select="$replacement"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select="$input"/>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

</xsl:transform>