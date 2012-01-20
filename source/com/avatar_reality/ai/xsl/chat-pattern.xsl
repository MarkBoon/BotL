<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

<xsl:output method="xml"/>
	
<xsl:strip-space elements="*"/>
		
<xsl:template match="/">
	<xsl:apply-templates select="*"/>
</xsl:template>

<xsl:template match="matches">
	<xsl:copy>
		<xsl:for-each select="@*">
			<xsl:choose>
				<xsl:when test="name() = 'chat-pattern'">
					<xsl:attribute name="pattern">
						<xsl:variable name="star0" select="'^(.*)\\s'"/>
						<xsl:variable name="star" select="'\\s(.*)'"/>
						<xsl:variable name="cond1" select="'\)\? '"/>
						<xsl:variable name="cond2" select="' )?'"/>
						<xsl:variable name="substitute-0" select="replace(.,'^_($| )',$star0)"/>
						<xsl:variable name="substitute-1" select="replace($substitute-0,' _($| )',$star)"/>
						<xsl:variable name="substitute-2" select="replace($substitute-1,$cond1,$cond2)"/>
						<xsl:call-template name="replace-variables">
							<xsl:with-param name="original" select="$substitute-2"/>
						</xsl:call-template>
					</xsl:attribute>
				</xsl:when>
				<xsl:when test="starts-with(name(),'ar-')">
					<xsl:variable name="function" select="concat('ar:',substring(name(),4))"/>
					<xsl:variable name="text" select="."/>
					<xsl:element name="return">
						<xsl:element name="use">
							<xsl:attribute name="name" select="$function"/>
						</xsl:element>
						<xsl:value-of select="$function"/>
						<xsl:text>("</xsl:text>
						<xsl:value-of select="$text"/>
						<xsl:text>")</xsl:text>
					</xsl:element>
				</xsl:when>
				<xsl:otherwise>
					<xsl:copy/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
		<xsl:apply-templates select="*"/>
		<xsl:apply-templates select="text()"/>
	</xsl:copy>
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

<xsl:template name="replace-variables">
	<xsl:param name="original"/>
	
	<xsl:variable name="variable" select="'.*?(~\w*).*'"/>
	
	<xsl:choose>
		<xsl:when test="matches($original,$variable)">
			<xsl:variable name="var-name" select="replace($original,$variable,'&#36;1')"/>
			<xsl:value-of select="concat(substring-before($original,$var-name),'\b')"/>
			<xsl:apply-templates select="//declare/variable[@name=$var-name]/@value"/>
			<xsl:call-template name="replace-variables">
				<xsl:with-param name="original" select="concat('\b',substring-after($original,$var-name))"/>
			</xsl:call-template>
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select="$original"/>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

</xsl:transform>