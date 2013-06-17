<h4>Visualisations</h4>

<table class="table table-bordered" style="background-color: rgb(249, 249, 249)">
    <tr>
        <td width="50%">
            <div class="visualisation" visLink="${createLink(action:'weedNonWeedBreakdownForVisit', params:[studyLocationVisitId: studyLocationVisitDetails.studyLocationVisitId])}"></div>
        </td>
        <td width="50%">
            <div class="visualisation" visLink="${createLink(action:'structuralSummaryForVisit', params:[studyLocationVisitId: studyLocationVisitDetails.studyLocationVisitId])}"></div>
        </td>
    </tr>
    <tr>
        <td>
            <div class="visualisation" visLink="${createLink(action:'soilECForVisit', params:[studyLocationVisitId: studyLocationVisitDetails.studyLocationVisitId])}"></div>
        </td>
        <td>
            <div class="visualisation" visLink="${createLink(action:'soilpHForVisit', params:[studyLocationVisitId: studyLocationVisitDetails.studyLocationVisitId])}"></div>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <div class="visualisation" visLink="${createLink(action:'pointInterceptTaxaForVisit', params:[studyLocationVisitId: studyLocationVisitDetails.studyLocationVisitId])}"></div>
        </td>
    </tr>



</table>

<script type="text/javascript">

    $(".visualisation").each(function() {
        var visLink =$(this).attr("visLink");
        if (visLink) {
            $(this).html('<img src="../images/spinner.gif" />&nbsp;Loading...');
            var targetElement = $("[visLink='" + visLink + "']");
            $.ajax(visLink).done(function(html) {
                targetElement.html(html);
            });

        }
    });

</script>