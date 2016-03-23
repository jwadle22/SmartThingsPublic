/**
 *  Even the temperature
 *
 *  Author: jaydenaphillips@gmail.com
 *  Date: 2014-01-11
 */

// Automatically generated. Make future change here.
definition(
    name: "Even The Temp",
    namespace: "devJayden",
    author: "jaydenaphillips@gmail.com",
    description: "Even the temp.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    oauth: true
)

preferences {
	section("When the temperature difference is too high") {
    input "temperatureSensors", "capability.temperatureMeasurement ", title: "Where?", multiple: true, required: true
	}
    section("By a difference of") {
    input "difference", "decimal", title: "Degrees", required: true
    }
    section("Turn the fan on") {
    input "thermostat", "capability.thermostat", title: "Where?", multiple: false, required: true
	}
    
    
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
    
}

def initialize() {

  subscribe(temperatureSensors, "temperature", executeCheck)
  subscribe(thermostat, "temperature", executeCheck)
  temperatureCheck()

}

def executeCheck(evt) {
	temperatureCheck()
	
}

def temperatureCheck() {  
	def turnOffFan = "True"  
    def actualDifference
	for (sensor in temperatureSensors) {
    	if ((sensor.currentTemperature - thermostat.currentTemperature) >= difference) {
        	log.debug "Turning on fan"
        	thermostat.setThermostatFanMode("on")
            turnOffFan = "False"
        } else if ((thermostat.currentTemperature - sensor.currentTemperature) >= difference) {
            log.debug "Turning on fan"
        	thermostat.setThermostatFanMode("on")
            turnOffFan = "False"
        	
        }
    }
    if (turnOffFan == "True") {
    	thermostat.setThermostatFanMode("auto")
        log.debug "Turning off fan"
    }
	
}