package au.org.ala.soils2sat

class EcologicalContext implements Serializable {

    String name
    String description
    static hasMany = [samplingUnits:SamplingUnit]

    static constraints = {
        name nullable: false, blank: false
        description nullable: true
    }

}