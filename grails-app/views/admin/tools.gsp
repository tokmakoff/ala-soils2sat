<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<!doctype html>
<html>
    <head>
        <r:require module='jqueryui'/>
        <meta name="layout" content="adminLayout"/>
        <title>Soils to Satellites - Admin - Tools</title>
    </head>

    <body>
        <script type="text/javascript">

            $(document).ready(function() {

                $("#btnClearLayersCache").click(function(e) {
                    e.preventDefault();
                    $.ajax("${createLink(controller: 'admin', action:'clearLayersCacheAjax')}").done(function(result) {
                        this.location = "${createLink(controller: 'admin', action:'tools')}";
                    });
                });

                $("#btnCreateDefaultCriteria").click(function(e) {
                    e.preventDefault();
                    window.location = "${createLink(controller: 'admin', action:'createDefaultSearchCriteria')}";
                });

                $("#btnClearAusPlotsCache").click(function(e) {
                    e.preventDefault();
                    $.ajax("${createLink(controller: 'admin', action:'clearAusPlotsCacheAjax')}").done(function(result) {
                        this.location = "${createLink(controller: 'admin', action:'tools')}";
                    });
                });

            });

        </script>
        <content tag="pageTitle">Tools</content>
        <table class="table table-bordered table-striped">
            <thead>
                <tr>
                    <th>Tool</th>
                    <th>Description</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>
                        <button id="btnClearLayersCache" class="btn btn-small btn-info">Clear&nbsp;Layers&nbsp;Cache</button>
                    </td>
                    <td>
                        Web service calls to the ALA Spatial Portal for Environmental Layer data are cached. This will clear the cache to allow updated layer data to be presented in the S2S Portal
                    </td>
                </tr>
                <tr>
                    <td>
                        <button id="btnClearAusPlotsCache" class="btn btn-small btn-info">Clear&nbsp;AusPlots&nbsp;Cache</button>
                    </td>
                    <td>
                        Web service calls to the AEKOS Portal for survey data are cached. This will clear the cache to allow updated survey data to be presented in the S2S Portal
                    </td>
                </tr>

                <tr>
                    <td>
                        <button id="btnCreateDefaultCriteria" class="btn btn-small btn-info">Create default search criteria</button>
                    </td>
                    <td>
                        Used when the database has been refreshed to put in a set of default search criteria
                    </td>
                </tr>

            </tbody>
        </table>
    </body>
</html>