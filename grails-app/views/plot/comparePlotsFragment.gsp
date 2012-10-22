<style type="text/css">

  .tab-content {
    border-left: 1px solid #d3d3d3;
    border-right: 1px solid #d3d3d3;
    border-bottom: 1px solid #d3d3d3;
    padding: 5px;
    background-color: white;
  }
</style>

<legend style="margin-bottom: 3px;">
<table style="width: 100%">
  <tr>
    <td>Compare Study Locations (${appState?.selectedPlots?.size()})</td>
    <td style="text-align: right">
      <button id="btnCompareExport" class="btn btn-small">Export as CSV</button>
    </td>
  </tr>
</table>
</legend>
<g:set var="max_width" value="790"/>
<g:set var="max_height" value="500"/>

<g:if test="${appState?.layers?.size() >= 1 && appState?.selectedPlots?.size() > 1}">
<div class="tabbable">
  <ul class="nav nav-tabs" style="margin-bottom: 0px">
    <li class="active"><a href="#layerData" data-toggle="tab">Layers</a></li>
    <li><a href="#taxaData" data-toggle="tab">Taxa</a></li>
  </ul>
  <div class="tab-content">

    <div class="tab-pane active" id="layerData" >
      <div style="max-height: ${max_height}px; max-width: ${max_width}px; overflow: scroll;">
        <table class="table table-striped table-condensed">
          <thead>
            <tr>
              <th></th>
              <g:each in="${appState.selectedPlots}" var="plot">
                <th>${plot.name}</th>
              </g:each>
            </tr>
          </thead>
          <tbody>
            <g:each in="${results.fieldNames}" var="fieldName">
              <tr>
                <td>${fieldName}</td>
                <g:each in="${appState?.selectedPlots}" var="plot">
                  <td>${results.data[plot.name][fieldName]}</td>
                </g:each>
              </tr>
            </g:each>
          </tbody>
        </table>
      </div>
    </div>

    <div class="tab-pane" id="taxaData">
      <div id="taxaContent" style="max-height: ${max_height}px; max-width: ${max_width}px; overflow: scroll;">
      </div>
    </div>

  </div>
</g:if>
  <g:else>
    <p>You must first select at least one environmental layer, and two or more study locations before using this feature.
    </p>
  </g:else>
</div>

<script type="text/javascript">
  $("#btnCompareExport").click(function(e) {
    e.preventDefault();
    location.href = "${createLink(controller: 'plot', action:'exportComparePlots')}";
  });

  $('a[data-toggle="tab"]').on('shown', function (e) {
    var tabHref = $(this).attr('href');
    if (tabHref == '#taxaData') {
      $("#taxaContent").html("Loading...");
      $.ajax("${createLink(controller:'plot', action:'compareTaxaFragment')}").done(function(content) {
        $("#taxaContent").html(content);
      });
    }

  });
</script>