<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="registration">
<html>
<head>
    <style>
        .container {
            width: 570px;
            min-height: 450px;
            background-color: rgb(238, 238, 238);
            text-align: center;
            padding: 10px;
            margin: auto;
        }

        .heading {
            height: 30px;
            margin-top: 5px;
        }

        fieldset {
            border: 1pt solid;
            border-color: rgb(184, 207, 229);
            margin-top: 10px;
        }

        table {
            margin-left: 25px;
        }

        input[type="text"] {
            width: 130px;
            height: 25px;
        }

        .labelFirstCol {
            width: 85px;
        }

        .labelSecondCol {
            width: 109px;
        }

        .inputBoxCol {
            width: 170px;
        }
    </style>
</head>

<body>
    <div class="container">
        <div class="heading">
            <strong>Registration</strong>
        </div>
        <form>
            <fieldset>
                <legend align="left">Buyer info</legend>
                <table>
                    <tr>
                        <td class="labelFirstCol">
                            <label>Name:</label>
                        </td>
                        <td class="inputBoxCol">
                            <input type="text">
                                <xsl:attribute name="value">
                                    <xsl:value-of select="buyer/name"/>
                                </xsl:attribute>
                                <xsl:attribute name="disabled">true</xsl:attribute>
                            </input>
                        </td>
                        <td class="labelSecondCol">
                            <label>Surname:</label>
                        </td>
                        <td class="inputBoxCol">
                            <input type="text">
                                <xsl:attribute name="value">
                                    <xsl:value-of select="buyer/surname"/>
                                </xsl:attribute>
                                <xsl:attribute name="disabled">true</xsl:attribute>
                            </input>
                        </td>
                    </tr>
                    <tr>
                        <td class="labelFirstCol">
                            <label>Email:</label>
                        </td>
                        <td class="inputBoxCol">
                            <input type="text">
                                <xsl:attribute name="value">
                                    <xsl:value-of select="buyer/email"/>
                                </xsl:attribute>
                                <xsl:attribute name="disabled">true</xsl:attribute>
                            </input>
                        </td>
                    </tr>
                </table>
            </fieldset>

            <fieldset>
                <legend align="left">Course info</legend>
                <table>
                    <tr>
                        <td class="labelFirstCol">
                            <label>Date:</label>
                        </td>
                        <td class="inputBoxCol">
                            <input type="text">
                                <xsl:attribute name="value">
                                    <xsl:value-of select="course/date"/>
                                </xsl:attribute>
                                <xsl:attribute name="disabled">true</xsl:attribute>
                            </input>
                        </td>
                        <td class="labelSecondCol">
                            <label>Time:</label>
                        </td>
                        <td class="inputBoxCol">
                            <input type="text">
                                <xsl:attribute name="value">
                                    <xsl:value-of select="course/time"/>
                                </xsl:attribute>
                                <xsl:attribute name="disabled">true</xsl:attribute>
                            </input>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <input type="checkbox">
                                <xsl:if test="course/onlinepayment='true'">
                                    <xsl:attribute name="checked"></xsl:attribute>
                                </xsl:if>
                                <xsl:attribute name="disabled">true</xsl:attribute>
                            </input>
                        </td>
                        <td align="left">Online payment</td>
                    </tr>
                </table>
            </fieldset>

            <fieldset>
                <legend align="left">Guest info</legend>
                <table>
                    <xsl:for-each select="guests/guest">
                        <tr>
                            <td class="labelFirstCol">
                                <label>Name:</label>
                            </td>
                            <td class="inputBoxCol">
                                <input type="text">
                                <xsl:attribute name="value">
                                    <xsl:value-of select="name"/>
                                </xsl:attribute>
                                <xsl:attribute name="disabled">true</xsl:attribute>
                            </input>
                            </td>
                            <td class="labelSecondCol">
                                <label>Surname:</label>
                            </td>
                            <td class="inputBoxCol">
                                <input type="text">
                                <xsl:attribute name="value">
                                    <xsl:value-of select="surname"/>
                                </xsl:attribute>
                                <xsl:attribute name="disabled">true</xsl:attribute>
                            </input>
                            </td>
                        </tr>
                    </xsl:for-each>
                </table>
            </fieldset>
        </form>
    </div>
</body>
</html>
</xsl:template>
</xsl:stylesheet>