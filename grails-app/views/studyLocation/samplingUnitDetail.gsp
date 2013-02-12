<!doctype html>
<html>
    <head>
        <r:require module='jqueryui'/>
        <r:require module='bootstrap_responsive'/>
        <meta name="layout" content="detail"/>
        <title>Sampling Unit details - ${studyLocationName} - Visit ${visitDetail.id} - ${samplingUnit}</title>
    </head>

    <body>
        <div class="container-fluid">
            <legend>
                <table style="width:100%">
                    <tr>
                        <td>Study Location&nbsp;&#187;&nbsp;
                            <a href="${createLink(controller: 'studyLocation', action: 'studyLocationSummary', params: [studyLocationName: studyLocationName])}">${studyLocationName}</a>&nbsp;&#187;&nbsp;
                            <a href="${createLink(controller: 'studyLocation', action: 'studyLocationVisitSummary', params: [studyLocationName: studyLocationName])}">Visits</a>&nbsp;&#187;&nbsp;
                            <a href="${createLink(controller: 'studyLocation', action: 'studyLocationVisitSamplingUnits', params: [studyLocationName: studyLocationName, visitId: visitDetail.id])}">Sampling Units</a>&nbsp;&#187;&nbsp;
                            ${samplingUnit}</td>
                        <td></td>
                    </tr>
                </table>
            </legend>

            <h4>Sampling Unit - ${samplingUnit}</h4>
            <table class="table table-bordered table-striped">
                <thead>
                    <tr>
                        <g:each in="${columnHeadings}" var="colHeading">
                            <th>${colHeading}</th>
                        </g:each>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${dataList}" var="row">
                        <tr>
                            <g:each in="${columnHeadings}" var="colHeading">
                                <td>${row[colHeading]}</td>
                            </g:each>
                        </tr>
                    </g:each>
                </tbody>
            </table>
        </div>
    </body>
</html>