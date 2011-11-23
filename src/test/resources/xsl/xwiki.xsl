<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<feed xmlns="http://www.w3.org/2005/Atom" xmlns:wrf="http://www.webreformatter.com/ns#">
			<title>
				<xsl:copy-of select="//title/*" />
			</title>
			<entry>
				<title>
					<xsl:value-of select="//div[@id='document-title']/h1" />
				</title>
				<wrf:copyright>
					<xsl:value-of select="//div[@id='xwikilicence']" />
				</wrf:copyright>
				<content type="xhtml">
					<div xmlns="http://www.w3.org/1999/xhtml">
						<xsl:apply-templates
							select="//div[@id='xwikicontent']/*" />
					</div>
				</content>
			</entry>
		</feed>
	</xsl:template>
    
 


	<!-- Table of Content -->
	
    <xsl:template match="div[@class='floatinginfobox']" />
    <xsl:template match="div[@class='box floatinginfobox']" />
    
	<xsl:template match="@*|node()" mode="remove" />
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>