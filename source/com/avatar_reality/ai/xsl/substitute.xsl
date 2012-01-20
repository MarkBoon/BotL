<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

	<xsl:output method="xml"/>
	
	<xsl:strip-space elements="*"/>

	<xsl:template match="/">
		<xsl:apply-templates select="*"/>
	</xsl:template>
	
	<xsl:template match="substitute">
		<xsl:element name="matches">
			<xsl:for-each select="@*">
				<xsl:choose>
					<xsl:when test="name() = 'with'">
						<xsl:element name="srai">'<xsl:value-of select="."/>'</xsl:element>
					</xsl:when>
					<xsl:otherwise>
						<xsl:copy/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
			<xsl:apply-templates select="*"/>
			<xsl:apply-templates select="text()"/>
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

</xsl:transform>