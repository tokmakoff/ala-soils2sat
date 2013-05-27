package au.org.ala.soils2sat

class VisualisationController {

    def studyLocationService
    def biocacheService
    def springSecurityService
    def layerService

    def studyLocationVisitVisualisations() {
        def studyLocationVisitDetails = studyLocationService.getVisitDetails(params.studyLocationVisitId)
        [studyLocationVisitDetails: studyLocationVisitDetails]
    }

    def structuralSummaryForVisit() {

        def columns = [['string', 'Species'], ['number', 'Upper'], ['number', 'Mid'], ['number', 'Ground']]
        def taxaMap = studyLocationService.getPointInterceptTaxaForVisit(params.studyLocationVisitId)
        def structuralSummary = studyLocationService.getSamplingUnitDetails(params.studyLocationVisitId, "4")?.samplingUnitData[0]

        def data = [
                ["U1 ${structuralSummary.upper1Dominant ?:''}", taxaMap[structuralSummary.upper1Dominant] ?: 0, 0, 0],
                ["U2 ${structuralSummary.upper2Dominant ?:''}", taxaMap[structuralSummary.upper2Dominant] ?: 0, 0, 0],
                ["U3 ${structuralSummary.upper2Dominant ?:''}", taxaMap[structuralSummary.upper3Dominant] ?: 0, 0, 0],
                ["U1 ${structuralSummary.mid1Dominant ?:''}", 0, taxaMap[structuralSummary.mid1Dominant] ?: 0, 0],
                ["U2 ${structuralSummary.mid2Dominant ?:''}", 0, taxaMap[structuralSummary.mid2Dominant] ?: 0, 0],
                ["U3 ${structuralSummary.mid3Dominant ?:''}", 0, taxaMap[structuralSummary.mid3Dominant] ?: 0, 0],
                ["U1 ${structuralSummary.ground1Dominant ?:''}", 0, 0, taxaMap[structuralSummary.ground1Dominant] ?: 0],
                ["U2 ${structuralSummary.ground2Dominant ?:''}", 0, 0, taxaMap[structuralSummary.ground2Dominant] ?: 0],
                ["U3 ${structuralSummary.ground3Dominant ?:''}", 0, 0, taxaMap[structuralSummary.ground3Dominant] ?: 0],
        ]

        def colors = ['#4E6228','#652524', '#4E81BD']

        render(view:'columnChart', model: [columns: columns, data: data, colors: colors, title: "Structural Summary", name:'structuralSummary', stacked: true])
    }

    public weedNonWeedBreakdownForVisit() {

        def weedList = biocacheService.getWeedsOfNationalSignificance()?.sort { it }

        def taxaMap = studyLocationService.getPointInterceptTaxaForVisit(params.studyLocationVisitId)
        def ausplotsNames = taxaMap.keySet().collect()

        // def ausplotsNames = studyLocationService.getVoucheredTaxaForStudyLocation(params.studyLocationVisitId)

        def weedCount = 0
        weedList.each { weedName ->
            def weed = ausplotsNames.find { it.trim()?.equalsIgnoreCase(weedName.trim()) }
            if (weed) {
                weedCount++
            }
        }

        def columns = [
            ['string', 'Label'], ['number', 'abundance']
        ]

        def data = [
            ['Non-Weed species', ausplotsNames.size() - weedCount],
            ['Weed species', weedCount]
        ]

        def colors = ['#99B958', '#BD4E4C']

        render(view:'weedNonWeedBreakdownForLocation', model: [columns: columns, data: data, colors: colors])
    }




    def studyLocationVisualisations() {

        [studyLocationName: params.studyLocationName]
    }

    def compareStudyLocationVisualisations() {

    }

    def compareLandformElement() {
        def userInstance = springSecurityService.currentUser as User
        def appState = userInstance?.applicationState
        def columns = [['string', 'Study Location'], ['number', 'Landform Element']]

        def elementMap = [:]
        appState.selectedPlotNames.each { studyLocationName ->
            def studyLocationDetails = studyLocationService.getStudyLocationDetails(studyLocationName)
            def landformElement = studyLocationDetails.landformElement ?: 'Unspecified'
            if (elementMap.containsKey(landformElement)) {
                elementMap[landformElement]++
            } else {
                elementMap[landformElement] = 1;
            }
        }

        def data = []
        elementMap.keySet().each {
            data << [it, elementMap[it]]
        }

        return [columns: columns, data: data]
    }

    def compareScalarLayer() {
        def userInstance = springSecurityService.currentUser as User
        def appState = userInstance?.applicationState
        def layerName = params.layerName
        def layerInfo = layerService.getLayerInfo(layerName)

        def title = layerInfo.displayname
        if (layerInfo.environmentalvalueunits) {
            title += " (${layerInfo.environmentalvalueunits})"
        }

        def columns = [['string', 'Study Location'], ['number', layerInfo.displayname]]
        def data = []

        appState.selectedPlotNames.each { studyLocationName ->
            def studyLocationDetails = studyLocationService.getStudyLocationDetails(studyLocationName)
            def values = layerService.getIntersectValues(studyLocationDetails.latitude, studyLocationDetails.longitude, [layerName])
            data << [studyLocationName, values[layerName] ?: 0]
        }

        def colors = [ '#4E81BD' ]

        return [columns: columns, data: data, colors: colors, layerInfo: layerInfo, title: title]
    }

    def compareDistinctSpecies() {
        def data = []
        def userInstance = springSecurityService.currentUser as User
        def appState = userInstance?.applicationState
        def colors = ['#99B958']
        def columns = [['string', 'Study Location'], ['number', 'Number of distinct species']]
        def nameMap = [:]
        appState.selectedPlotNames.each {
            nameMap[it] = studyLocationService.getVoucheredTaxaForStudyLocation(it)
        }

        def distinctMap = [:]
        appState.selectedPlotNames?.each { studyLocation ->
            def newList = []
            def candidateList = nameMap[studyLocation]
            candidateList.each { taxa ->
                def include = true
                nameMap.each { kvp ->
                    if (include && kvp.key != studyLocation) {
                        if (kvp.value.contains(taxa)) {
                            include = false
                        }
                    }
                }
                if (include) {
                    newList << taxa
                }
            }
            distinctMap[studyLocation] = newList
        }

        appState.selectedPlotNames.each {
            data << [it, distinctMap[it]?.size()]
        }

        [data: data, colors: colors, columns: columns]
    }

    def plantSpeciesBreakdownBySource() {

        def columns = [['string',"Label"],['number', "%"]]

        def ausplotsNames = studyLocationService.getVoucheredTaxaForStudyLocation(params.studyLocationName)
        def studyLocationDetails = studyLocationService.getStudyLocationDetails(params.studyLocationName)
        def alaNames = biocacheService.getTaxaNamesForLocation(studyLocationDetails.latitude, studyLocationDetails.longitude)
        def both = []
        alaNames.each {
            if (ausplotsNames.contains(it)) {
                both.add(it)
            }
        }

        both.each {
            ausplotsNames.remove(it)
            alaNames.remove(it)
        }

        def data = [
            ["Both AusPlots & ALA", both.size()],
            ["AusPlots only", ausplotsNames.size()],
            ["ALA Only", alaNames.size()]
        ]

        [columns: columns, data: data]
    }

    def soilECForLocation() {

        def columns = [
            ['string',"Depth"],
            ['number', "Soil EC"]
        ]

        def samplingUnitData = studyLocationService.getSoilECForStudyLocation(params.studyLocationName)
        def data = samplingUnitData?.collect { ["${it.upperDepth} - ${it.lowerDepth}", it.EC ]}


        // is there at least one row with non-null data?
        def nonNull = data.find { it[1] != null }
        if (!nonNull) {
            data = []
        }

        def colors = [ '#4E81BD' ]

        return [columns: columns, data: data, colors: colors]
    }

    def soilpHForLocation() {

        def litmusColors = getLitmusColors()

        def columns = [['string',"Depth"]]
        litmusColors.each {
            columns << ['number', "pH ${it.pH}"]
        }

        def realData = studyLocationService.getSoilPhForStudyLocation(params.studyLocationName)

        def data = realData?.collect { [depth: "${it.upperDepth} - ${it.lowerDepth}", ph: it.pH ]}

        def adjustedData = []

        // is there at least one row with non-null data?
        def nonNull = data.find { it.ph != null }
        if (!nonNull) {
            data = []
        }

        data?.each { element ->
            def row = [element.depth]
            boolean found = false
            litmusColors.each { color ->
                if (!found && color.pH > element.ph) {
                    found = true;
                    if (row.size() > 1) {
                        row.pop()
                    }
                    row << element.ph
                    row << 0
                } else {
                    row << 0
                }
            }
            adjustedData << row
        }

        def colors = litmusColors.collect { it.color }

        [columns: columns, data: adjustedData, colors: colors]
    }

    public weedNonWeedBreakdownForLocation() {

        def weedList = biocacheService.getWeedsOfNationalSignificance()?.sort { it }

        def ausplotsNames = studyLocationService.getVoucheredTaxaForStudyLocation(params.studyLocationName)

        def weedCount = 0
        weedList.each { weedName ->
            def weed = ausplotsNames.find { it.trim()?.equalsIgnoreCase(weedName.trim()) }
            if (weed) {
                weedCount++
            }
        }


        def columns = [
            ['string', 'Label'], ['number', 'abundance']
        ]

        def data = [
            ['Non-Weed species', ausplotsNames.size() - weedCount],
            ['Weed species', weedCount]
        ]

        def colors = ['#99B958', '#BD4E4C']

        [columns: columns, data: data, colors: colors]
    }

    private static getLitmusColors() {
        def litmusColors = [
            [pH:0, color:'#F61800'],
            [pH:1, color:'#F76502'],
            [pH:2, color:'#FCCC00'],
            [pH:3, color:'#FFFF02'],
            [pH:4, color:'#CCFF33'],
            [pH:5, color:'#56FF00'],
            [pH:6, color:'#5AB700'],
            [pH:7, color:'#1D6632'],
            [pH:8, color:'#2E9965'],
            [pH:9, color:'#36B7BE'],
            [pH:10, color:'#3398FF'],
            [pH:11, color:'#0066FF'],
            [pH:12, color:'#0000FF'],
            [pH:13, color:'#000099'],
            [pH:14, color:'#663266'],
        ]
        return litmusColors
    }


}
