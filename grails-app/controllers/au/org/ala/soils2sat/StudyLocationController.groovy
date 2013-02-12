package au.org.ala.soils2sat

import grails.converters.JSON
import au.com.bytecode.opencsv.CSVWriter
import java.util.zip.ZipOutputStream
import java.util.zip.ZipEntry

class StudyLocationController {

    def springSecurityService
    def studyLocationService
    def biocacheService
    def layerService

    def getPlots() {
        def results = studyLocationService.getStudyLocations()

        render (results as JSON)
    }

    def getSelectedPlots() {
        def user = springSecurityService.currentUser as User
        def candidates = studyLocationService.getStudyLocations()
        def results = []
        if (user) {
            candidates.each {
                if (user.applicationState?.containsPlot(it.siteName)) {
                    results.add(it)
                }
            }
        }

        render (results as JSON)
    }

    def getUserDisplayedPlots() {
        def user = springSecurityService.currentUser as User
        def candidates = studyLocationService.getStudyLocations()
        def results = []
        def plotSelectedOnly = user && user.applicationState?.plotOnlySelectedLocations
        candidates.each {
            it.selected = user.applicationState?.containsPlot(it.siteName)
            if (!plotSelectedOnly || it.selected) {
                results << it
            }
        }

        // Sort the results so that selected plots get rendered last, and therefore on top...
        results = results.sort { it.selected }

        render (results as JSON)
    }

    def synopsisFragment() {
        def studyLocationName = params.studyLocationName;
        def userInstance = springSecurityService.currentUser as User

        [studyLocationName:studyLocationName, userInstance: userInstance, appState: userInstance?.applicationState]
    }


    def studyLocationDataFragment() {

        def userInstance = springSecurityService.currentUser as User
        def appState = userInstance?.applicationState

        def layerNames = appState.layers.collect({ it.name }).join(",")

        def studyLocationName = params.studyLocationName

        def studyLocation = studyLocationService.getStudyLocationSummary(studyLocationName)
        def results = []
        if (layerNames && studyLocation) {
            def url = new URL("${grailsApplication.config.spatialPortalRoot}/ws/intersect/${layerNames}/${studyLocation.latitude}/${studyLocation.longitude}")
            results = JSON.parse(url.text)

        }

        [results:results, userInstance: springSecurityService.currentUser, appState: appState, studyLocation: studyLocation]
    }

    private Map getCompareData(User userInstance) {
        def appState = userInstance?.applicationState

        def data =[:]
        def fieldNames = ['latitude', 'longitude']
        def fieldUnits = [:]
        if (userInstance && appState?.layers && appState?.selectedPlots && appState?.selectedPlots.size() > 1) {
            def layerNames = appState.layers.collect({ it.name }).join(",")
            for (StudyLocation studyLocation : appState.selectedPlots) {
                def studyLocationSummary = studyLocationService.getStudyLocationSummary(studyLocation.name)
                def url = new URL("${grailsApplication.config.spatialPortalRoot}/ws/intersect/${layerNames}/${studyLocationSummary.latitude}/${studyLocationSummary.longitude}")
                def studyLocationResults = JSON.parse(url.text)
                def temp = [:]
                temp.latitude = studyLocationSummary.latitude
                temp.longitude = studyLocationSummary.longitude

                studyLocationResults.each {
                    println it
                    def fieldName = it.layername
                    if (!fieldNames.contains(fieldName)) {
                        fieldNames << fieldName
                    }
                    temp[fieldName] = it.value
                    fieldUnits[fieldName] = it.units
                }
                data[studyLocation.name] = temp
            }
        }

        return [data: data, fieldNames: fieldNames, fieldUnits: fieldUnits]
    }

    def compareStudyLocationsFragment = {
        def userInstance = springSecurityService.currentUser as User
        def appState = userInstance?.applicationState
        def results = getCompareData(userInstance)
        [userInstance: userInstance, results: results, appState: appState ]
    }

    def compareTaxaFragment = {

        def userInstance = springSecurityService.currentUser as User
        def appState = userInstance?.applicationState

        def results = [:]

        appState.selectedPlots.each { studyLocation ->
            def studyLocationSummary = studyLocationService.getStudyLocationSummary(studyLocation.name)
            def studyLocationTaxaList = biocacheService.getTaxaNamesForLocation(studyLocationSummary.latitude, studyLocationSummary.longitude, 10, params.rank ?: 'family')
            results[studyLocation.name] = studyLocationTaxaList
        }

        if (params.diffMode?.toLowerCase() == 'intersect') {
            def candidateEntry = results.max({ it.value.size() })

            def newList = []
            candidateEntry.value.each { taxa ->
                def include = true
                appState.selectedPlots.each { studyLocation ->
                    if (studyLocation.name != candidateEntry.key) {
                        def list = results[studyLocation.name]
                        if (!list.contains(taxa)) {
                            include = false
                        }
                    }
                }

                if (include) {
                    newList << taxa
                }
            }

            appState.selectedPlots.each { studyLocation ->
                results[studyLocation.name] = newList
            }

        } else if (params.diffMode?.toLowerCase() == 'inverseintersect') {

            def newResults = [:]

            appState.selectedPlots.each { studyLocation ->
                def newList = []
                def candidateList = results[studyLocation.name]
                candidateList.each { taxa ->
                    def include = true
                    results.each { kvp ->
                        if (kvp.key != studyLocation.name) {
                            if (kvp.value.contains(taxa)) {
                                include = false
                            }
                        }
                    }
                    if (include) {
                        newList << taxa
                    }
                }
                newResults[studyLocation.name] = newList
            }
            results = newResults
        }

        [results:results, appState: appState, userInstance: userInstance]
    }

    def exportComparePlots = {

        def userInstance = springSecurityService.currentUser as User
        def appState = userInstance?.applicationState


        response.setHeader("Content-Disposition", "attachment;filename=CompareExport.zip")

        response.setContentType("application/zip")

        // First up write out the main tasks file -all the remaining fields are single value only
        def zipStream = new ZipOutputStream(response.getOutputStream())
        OutputStream bos = new BufferedOutputStream(zipStream);
        OutputStreamWriter outputwriter = new OutputStreamWriter(bos);
        CSVWriter writer = new CSVWriter(outputwriter);
        // Layer data
        zipStream.putNextEntry(new ZipEntry("environmentalLayers.csv"));
        exportCompareLayers(writer, userInstance)
        writer.flush()
        zipStream.closeEntry();
        // Taxa data
        zipStream.putNextEntry(new ZipEntry("taxa.csv"));
        exportCompareTaxa(writer, userInstance)
        writer.flush()
        zipStream.closeEntry();

        // clean up
        zipStream.close();
    }

    private exportCompareTaxa(CSVWriter writer, User userInstance) {
        def appState = userInstance?.applicationState

        def results = [:]

        appState.selectedPlots.each { studyLocation ->
            def studyLocationSummary = studyLocationService.getStudyLocationSummary(studyLocation.name)
            def studyLocationTaxaList = biocacheService.getTaxaNamesForLocation(studyLocationSummary.latitude, studyLocationSummary.longitude, 10, params.rank ?: 'family')
            results[studyLocation.name] = studyLocationTaxaList
        }

        def columnHeaders = results.keySet().toArray() as String[]
        writer.writeNext(columnHeaders)

        def finished = false

        def rowIndex = 0
        while (!finished) {
            finished = true
            def values= []
            appState.selectedPlots.each { studyLocation ->
                def value = null
                def fieldList = results[studyLocation.name] as List
                if (fieldList.size() > rowIndex) {
                    value = fieldList[rowIndex]
                    finished = false;
                }
                values << value ?: ''
            }
            if (!finished) {
                writer.writeNext(values as String[])
            }
            rowIndex++
        }
    }

    private exportCompareLayers(CSVWriter writer, User userInstance) {

        def appState = userInstance.applicationState
        def results = getCompareData(userInstance)
        def columnHeaders = ["field"]
        appState.selectedPlots.each {
            columnHeaders << it.name
        }
        writer.writeNext(columnHeaders as String[])
        results.fieldNames.each { fieldName ->
            def lineItems = [fieldName]
            appState.selectedPlots.each { studyLocation ->
                def value = results.data[studyLocation.name][fieldName]
                lineItems << value ?: ''
            }
            writer.writeNext(lineItems as String[])
        }
    }

    def ajaxSetStudyLocationSelectedOnly() {
        def visibility = params.boolean("plotSelected") ?: false
        def userInstance = springSecurityService.currentUser as User
        def success = false;
        if (userInstance) {
            def appState = userInstance.applicationState
            appState.lock()
            appState.plotOnlySelectedLocations = visibility
            userInstance.save(flush: true, failOnError: true)
            success = true
        }
        render([status: success ? 'ok' : 'failed'] as JSON)
    }

    def deselectStudyLocation() {
        def studyLocationName = params.studyLocationName
        def success = false
        if (studyLocationName) {
            def userInstance = springSecurityService.currentUser as User
            def appState = userInstance?.applicationState
            appState.lock()
            def existing = appState?.selectedPlots?.find {
                it.name == studyLocationName
            }
            if (existing) {
                appState.removeFromSelectedPlots(existing)
                userInstance.save(flush: true)
                success = true
            }
        }
        render([status:success ? 'ok' : 'failed'] as JSON)
    }

    def selectStudyLocation() {
        def studyLocationName = params.studyLocationName
        def success = false
        if (studyLocationName) {
            def userInstance = springSecurityService.currentUser as User
            def appState = userInstance?.applicationState

            appState.lock()

            def existing = appState?.selectedPlots?.find {
                it.name == studyLocationName
            }
            if (!existing) {
                def studyLocation = new StudyLocation(name:studyLocationName)
                appState.addToSelectedPlots(studyLocation)
                appState.save(flush: true)
                success = true
            }
        }
        render([status:success ? 'ok' : 'failed'] as JSON)
    }

    def selectStudyLocations() {
        def studyLocationNames = params.studyLocationNames?.split(",");
        def success = false
        if (studyLocationNames) {
            def userInstance = springSecurityService.currentUser as User
            def appState = userInstance.applicationState
            appState.lock()
            studyLocationNames.each { studyLocationName ->
                def existing = appState?.selectedPlots?.find {
                    it.name == studyLocationName
                }
                if (!existing) {
                    def studyLocation = new StudyLocation(name:studyLocationName)
                    appState.addToSelectedPlots(studyLocation)
                }
            }
            appState?.save(flush: true, failOnError: true)
            success = true
        }
        render([status: success ? 'ok' : 'failed'] as JSON)
    }

    def clearSelectedStudyLocations() {
        def success = false
        def userInstance = springSecurityService.currentUser as User
        def appState = userInstance?.applicationState
        if (appState?.selectedPlots) {
            appState.selectedPlots.clear();
            appState.save(flush: true)
        }
        render([status:success ? 'ok' : 'failed'] as JSON)
    }

    def studyLocationSummary() {
        def studyLocationName = params.studyLocationName
        def studyLocationSummary = studyLocationService.getStudyLocationSummary(studyLocationName)
        def userInstance = springSecurityService.currentUser as User
        def appState = userInstance?.applicationState

        def isSelected = appState.selectedPlots.find {
            it.name == studyLocationName
        }

        [studyLocationSummary:studyLocationSummary, studyLocationName: studyLocationName, isSelected: isSelected != null]
    }

    def studyLocationVisitSummary() {
        def studyLocationName = params.studyLocationName
        def studyLocationSummary = studyLocationService.getStudyLocationSummary(studyLocationName)
        def visitSummaries = studyLocationSummary.visitSummaries

        [studyLocation:studyLocationSummary, studyLocationName: studyLocationName, visitSummaries: visitSummaries ]
    }

    def studyLocationLayersFragment() {

        def userInstance = springSecurityService.currentUser as User
        def appState = userInstance?.applicationState

        def studyLocationName = params.studyLocationName
        def studyLocationSummary = studyLocationService.getStudyLocationSummary(studyLocationName)
        def layerData = [:]
        if (studyLocationSummary) {
            def layerNames = appState.layers.collect({ it.name }).join(",")
            def url = new URL("${grailsApplication.config.spatialPortalRoot}/ws/intersect/${layerNames}/${studyLocationSummary.latitude}/${studyLocationSummary.longitude}")
            def studyLocationResults = JSON.parse(url.text)
            studyLocationResults.each {
                def fieldName = it.layername ?: it.field
                layerData[fieldName] = "${it.value}${it.units? ' (' + it.units + ')' :''}"
            }
        }

        [layerData: layerData, studyLocationName: studyLocationName, studyLocationSummary: studyLocationSummary]
    }

    def studyLocationTaxaFragment() {
        def userInstance = springSecurityService.currentUser as User
        def appState = userInstance?.applicationState

        def studyLocationName = params.studyLocationName
        def studyLocationSummary = studyLocationService.getStudyLocationSummary(studyLocationName)

        def rank = params.rank ?: 'family';
        int radius = params.int("radius") ?: 10

        def studyLocationTaxaList = biocacheService.getTaxaNamesForLocation(studyLocationSummary.latitude, studyLocationSummary.longitude, radius, rank)

        [studyLocationName: studyLocationName, studyLocationSummary: studyLocationSummary, taxaList: studyLocationTaxaList, rank: rank, radius: radius]
    }

    def studyLocationVisitSamplingUnits() {
        def userInstance = springSecurityService.currentUser as User
        def appState = userInstance?.applicationState

        def studyLocationName = params.studyLocationName
        def studyLocationSummary = studyLocationService.getStudyLocationSummary(studyLocationName)
        def visitSummary = studyLocationSummary.visitSummaries?.find {
            it.visitId == params.visitId
        }
        def visitDetail = studyLocationService.getVisitDetails(params.visitId as String)

        [studyLocationName: studyLocationName, studyLocationSummary: studyLocationSummary, visitDetail: visitDetail, visitSummary: visitSummary]
    }

    def ajaxSelectedStudyLocationsFragment() {
        def userInstance = springSecurityService.currentUser as User
        [userInstance: userInstance, appState: userInstance?.applicationState]
    }

    def samplingUnitDetail() {

        def visitId = params.visitId
        def samplingUnit = params.samplingUnit
        def studyLocationSummary = studyLocationService.getStudyLocationSummary(params.studyLocationName)
        def visitDetail = studyLocationService.getVisitDetails(visitId as String)
        def dataList = []
        switch (samplingUnit) {
            case "POINT_INTERCEPT":
                dataList = visitDetail.pointInterceptWithHerbIdAddedList
                break
            case "STRUCTURAL_SUMMARY":
                dataList = visitDetail.structuralSummaryList
                break
            case "SOIL_STRUCTURE":
                dataList = visitDetail.soilStructureList
                break
            case "SOIL_CHARACTER":
                dataList = visitDetail.soilCharacterisationList
                break
            case "SOIL_SAMPLING":
                dataList = visitDetail.soilSampleList
                break
            default:
            break;
        }

        def colHeadings = dataList[0]?.collect { it.key }

        [visitDetail: visitDetail, studyLocationName: params.studyLocationName, studyLocationSummary: studyLocationSummary, samplingUnit: samplingUnit, columnHeadings: colHeadings, dataList: dataList]
    }

}
