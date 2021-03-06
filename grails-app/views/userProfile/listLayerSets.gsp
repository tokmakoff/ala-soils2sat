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
        <meta name="layout" content="profilePage"/>
        <title>Layer Sets</title>
    </head>

    <body>
        <content tag="pageTitle">Layer Sets</content>
        <content tag="profileButtonBar">
            <button id="btnAddLayerSet" class="btn btn-small btn-primary pull-right"><i class="icon-plus icon-white"></i>&nbsp;Add Layer Set</button>
        </content>

        <table class="table table-bordered table-striped">
            <thead>
                <tr>
                    <th>Set Name</th>
                    <th>Layer count</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${layerSets}" var="layerSet">
                    <tr layerSetId="${layerSet.id}">
                        <td>${StringEscapeUtils.escapeHtml(layerSet.name)}</td>
                        <td>${layerSet.layers?.size()}</td>
                        <td>
                            <button class="btn btn-mini btn-danger btnDeleteLayerSet"><i class="icon-remove icon-white"></i>&nbsp;delete</button>&nbsp;
                            <button class="btn btn-mini btnEditLayerSet"><i class="icon-edit"></i>&nbsp;edit</button>
                        </td>
                    </tr>
                </g:each>
            </tbody>
        </table>

        <script type="text/javascript">

            $("#btnAddLayerSet").click(function (e) {
                e.preventDefault();
                window.location = "${createLink(controller:'userProfile', action:'newLayerSet')}";
            });

            $(".btnDeleteLayerSet").click(function (e) {
                e.preventDefault();
                var layerSetId = $(this).parents("tr[layerSetId]").attr("layerSetId");
                if (layerSetId) {
                    if (confirm("Are you sure you wish to delete this layer set?")) {
                        window.location = "${createLink(controller:'userProfile', action:'deleteLayerSet')}?layerSetId=" + layerSetId;
                    }
                }
            });

            $(".btnEditLayerSet").click(function (e) {
                e.preventDefault();
                var layerSetId = $(this).parents("tr[layerSetId]").attr("layerSetId");
                if (layerSetId) {
                    window.location = "${createLink(controller:'userProfile', action:'editLayerSet')}?layerSetId=" + layerSetId;
                }
            });


        </script>
    </body>
</html>