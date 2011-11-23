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
					<xsl:value-of select="//div[@id='content']/h1[@id='firstHeading']" />
				</title>
				<wrf:copyright>
					<xsl:value-of select="//div[@id='siteSub']" />
				</wrf:copyright>
				<content type="xhtml">
					<div xmlns="http://www.w3.org/1999/xhtml">
						<xsl:apply-templates
							select="//div[@id='content']/div[@id='bodyContent']/*" />
					</div>
				</content>
			</entry>
		</feed>
	</xsl:template>

	<xsl:template match="span[@id='coordinates-title']" />
	<xsl:template match="span[@id='coordinates']">
		<div class="wrf-geo">
			<span class="wrf-latitude">
				<xsl:value-of
					select=".//span[@class='geo-nondefault']//span[@class='latitude']" />
			</span>
			/
			<span class="wrf-longitude">
				<xsl:value-of
					select=".//span[@class='geo-nondefault']//span[@class='longitude']" />
			</span>
		</div>
	</xsl:template>
	<xsl:template match="span[@class='plainlinksneverexpand']" />
	<xsl:template match="span[contains(@class, 'geo-')]" />

	<xsl:template match="script" />
	<xsl:template match="*[@class='editsection']" />
	<xsl:template match="*[@id='jump-to-nav']" />
	<xsl:template match="*[contains(@class, 'navigation-only')]" />
	<xsl:template match="*[contains(@class, 'noprint')]" />

	<xsl:template match="*[@id='siteSub']" />
	<xsl:template match="*[@id='contentSub']" />
	<xsl:template match="*[@class='printfooter']" />
	<xsl:template match="*[@class='hiddenStructure']" />
	<!-- Related Categories -->
	<!-- <xsl:template match="*[@class='catlinks']" /> -->
	<!-- Related Portals -->
	<xsl:template match="*[@class='bandeau-portail']" />
	<!-- Infobox (right-side info) -->
	<xsl:template match="table[contains(@class, 'infobox')]">
		<div class="wrf-block wrf-infobox">
			<table class="wrf-metadata">
				<xsl:apply-templates select="*" />
			</table>
		</div>
	</xsl:template>

	<!-- Local References -->
	<!-- Kill the [ and ] symbols in local references -->
	<xsl:template match="sup[@class='reference']/a/span" />
	<xsl:template match="sup[@class='reference']">
		<sup class="wrf-ref wrf-local">
			<xsl:attribute name="id">
                <xsl:value-of select="@id" />
            </xsl:attribute>
			<xsl:apply-templates select="*" />
		</sup>
	</xsl:template>

	<!-- Remove new links but keep their content. -->
	<xsl:template match="i/a[@class='new']">
		<xsl:apply-templates select="node()" />
	</xsl:template>

	<!-- Right and left blocks -->
	<xsl:template match="div[@class='thumbinner']">
		<div class="wrf-block">
			<xsl:apply-templates select="." mode="infoblock" />
		</div>
	</xsl:template>
	<xsl:template match="div[@class='thumb tright']">
		<div class="wrf-block wrf-right">
			<xsl:apply-templates select="." mode="infoblock" />
		</div>
	</xsl:template>
	<xsl:template match="div[@class='thumb tleft']">
		<div class="wrf-block wrf-left">
			<xsl:apply-templates select="." mode="infoblock" />
		</div>
	</xsl:template>
	<xsl:template match="div" mode="infoblock">
		<div class="wrf-image">
			<xsl:copy-of select=".//a[@class='image']/img">
				<xsl:apply-templates select="@*|node()" />
			</xsl:copy-of>
		</div>
		<div class="wrf-description">
			<xsl:for-each
				select=".//div[@class='thumbcaption']/* | .//div[@class='thumbcaption']/text()">
				<xsl:choose>
					<xsl:when test="text()">
						<xsl:copy-of select="." />
					</xsl:when>
					<xsl:when test="div[@class='magnify']"></xsl:when>
					<xsl:when test="a[@class='internal']"></xsl:when>
					<xsl:otherwise>
						<xsl:apply-templates select="." />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</div>
	</xsl:template>

	<!-- Table of Content -->
	<!-- <xsl:template match="table[@class='toc']"> <div class="wrf-block wrf-right 
		menu vmenu"> <xsl:apply-templates select=".//ul" /> </div> </xsl:template> -->
	<xsl:template match="table[@class='toc']" />
	<xsl:template match="@*|node()" mode="remove" />
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>