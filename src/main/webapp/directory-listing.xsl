<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0">

  <xsl:output method="html" encoding="UTF-8" indent="no" doctype-system="about:legacy-compat"/>

  <xsl:template match="listing">
    <table style="width: 100%;">
      <tr>
        <th style="text-align: left;">Filename</th>
        <th style="text-align: center;">Size</th>
        <th style="text-align: right;">Last Modified</th>
      </tr>
      <xsl:apply-templates select="entries"/>
    </table>
    <xsl:apply-templates select="readme"/>
  </xsl:template>

  <xsl:template match="entries">
    <xsl:apply-templates select="entry"/>
  </xsl:template>

  <xsl:template match="readme">
    <hr style="height: 1px;" />
    <pre><xsl:apply-templates/></pre>
  </xsl:template>

  <xsl:template match="entry">
    <tr>
      <td style="text-align: left;">
        <xsl:variable name="urlPath" select="@urlPath"/>
        <a href="{$urlPath}">
          <pre><xsl:apply-templates/></pre>
        </a>
      </td>
      <td style="text-align: right;">
        <pre><xsl:value-of select="@size"/></pre>
      </td>
      <td style="text-align: right;">
        <pre><xsl:value-of select="@date"/></pre>
      </td>
    </tr>
  </xsl:template>

</xsl:stylesheet>
