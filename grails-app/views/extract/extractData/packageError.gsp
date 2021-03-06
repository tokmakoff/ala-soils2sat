%{--
  - ﻿Copyright (C) 2013 Atlas of Living Australia
  - All Rights Reserved.
  -
  - The contents of this file are subject to the Mozilla Public
  - License Version 1.1 (the "License"); you may not use this file
  - except in compliance with the License. You may obtain a copy of
  - the License at http://www.mozilla.org/MPL/
  -
  - Software distributed under the License is distributed on an "AS
  - IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  - implied. See the License for the specific language governing
  - rights and limitations under the License.
  --}%

<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="detail"/>
        <title>Extract Data from Study Location Visits</title>
    </head>

    <body>

        <style type="text/css">
        </style>

        <script type="text/javascript">

            $(document).ready(function () {
            });

        </script>

        <div class="container">
            <legend>
                <table style="width:100%">
                    <tr>
                        <td>
                            <sts:homeBreadCrumb />
                            <sts:navSeperator/>
                            <span class="sts-breadcrumb">Extract Data from selected Study Location Visits</span>
                        </td>
                        <td></td>
                    </tr>
                </table>
            </legend>

            <div class="row">
                <div class="span12">
                    <g:form>
                        <div class="well well-small">
                        <h4>Data Extraction Failed</h4>
                            <div class="alert alert-error">

                                <g:set var="errorSource" value="" />
                                <g:if test="${packageException instanceof au.org.ala.soils2sat.DOIMintingFailedException}">
                                    <g:set var="errorSource" value="from the TERN DOI Service" />
                                </g:if>

                                The data extraction has failed. The error returned ${errorSource} was:
                                <br/>
                                <br/>
                                <strong>${packageException?.message}</strong>
                            </div>
                        </div>

                        <g:link class="btn btn-small btn-primary" event="finish">Return to Map</g:link>

                    </g:form>
                </div>
            </div>
        </div>
    </body>
</html>
