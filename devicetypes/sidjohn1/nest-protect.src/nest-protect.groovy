/**
 *  Nest Protect
 *  Author: nick@nickhbailey.com
 *  Author: dianoga7@3dgo.net
 *  Date: 2015.08.30
 *
 * INSTALLATION
 * =========================================
 * 1) Create a new device type (https://graph.api.smartthings.com/ide/devices)
 *     Name: Nest Protect
 *     Author: nick@nickhbailey.com
 *     Author: dianoga7@3dgo.net
 *     Capabilities:
 *         Smoke
 *         Carbon Monixide
 *         Battery
 *         Polling
 *
 * 2) Create a new device (https://graph.api.smartthings.com/device/list)
 *     Name: Your Choice
 *     Device Network Id: Your Choice
 *     Type: Nest Protect (should be the last option)
 *     Location: Choose the correct location
 *     Hub/Group: Leave blank
 *
 * 3) Update device preferences
 *     Click on the new device to see the details.
 *     Click the edit button next to Preferences
 *     Fill in your information.
 *     To find your serial number, login to http://home.nest.com. Click on the smoke detector
 *     you want to see. Under settings, go to Technical Info. Your serial number is
 *     the second item.
 *
 * Copyright (C) 2014 Nick Bailey <nick@nickhbailey.com>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following
 * conditions: The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Overall inspiration and Authentication methods originally developed by Brian Steere
 * as part of the Nest thermostat device type: https://gist.github.com/Dianoga/6055918
 * and are subject to the following:
 *
 * Copyright (C) 2013 Brian Steere <dianoga7@3dgo.net>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following
 * conditions: The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *	Version: 1.0 - Initial Nickh Bailey Version
 *	Version: 1.1 - Cleaned up code, fixed "unknown" error for andriod devices, updated authentication, layout, and logging.
 */

preferences {
	input("username", "text", title: "Username", description: "Your Nest username (usually an email address)", required: true, displayDuringSetup: true)
	input("password", "password", title: "Password", description: "Your Nest password", required: true, displayDuringSetup: true)
	input("mac", "text", title: "MAC Address", description: "The MAC address of your smoke detector", required: true, displayDuringSetup: true)
}

 // for the UI
metadata {
	definition (name: "Nest Protect", author: "dianoga7@3dgo.net", namespace: "sidjohn1") {
		capability "Polling"
		capability "Battery"
		capability "Smoke Detector"
		capability "Carbon Monoxide Detector"
        
		attribute "alarmState", "string"
    }

    simulator {
        // TODO: define status and reply messages here
    }

    tiles {
		standardTile("alarmState", "device.alarmState", width: 1, height: 1) {
			state ("default", label:'--', icon: "st.unknown.unknown.unknown")
			state("clear", label:"clear", icon:"st.alarm.smoke.clear", backgroundColor:"#44B621")
			state("smoke", label:"smoke", icon:"st.alarm.smoke.smoke", backgroundColor:"#e86d13")
			state("co", label:"co", icon:"st.alarm.carbon-monoxide.carbon-monoxide", backgroundColor:"#e86d13")
		}
		standardTile("smoke", "device.smoke", width: 3, height: 3) {
			state ("default", label:'unknown', icon: "st.unknown.unknown.unknown")
			state("clear", label:"smoke: ok", icon:"st.alarm.smoke.clear", backgroundColor:"#44B621")
			state("detected", label:"smoke", icon:"st.alarm.smoke.smoke", backgroundColor:"#e86d13")
		}
		standardTile("carbonMonoxide", "device.carbonMonoxide", width: 1, height: 1){
			state ("default", label:'unknown', icon: "st.unknown.unknown.unknown")
			state("clear", label:"co: ok", icon:"st.particulate.particulate.particulate", backgroundColor:"#44B621")
			state("detected", label:"co", icon:"st.particulate.particulate.particulate", backgroundColor:"#e86d13")
		}
		standardTile("battery", "device.battery", width: 1, height: 1) {
			state ("default", label:'unknown', icon: "st.unknown.unknown.unknown")
			state("ok", label: "Battery: OK", backgroundColor: "#44B621")
			state("low", label: "Battery: Low", backgroundColor: "#e86d13")
		}
		standardTile("refresh", "device.smoke", width: 1, height: 1, inactiveLabel: false, canChangeIcon: false, decoration: "flat") {
			state("default", label:'refresh', action:"polling.poll", icon:"st.secondary.refresh-icon")
		}

	main "alarmState"
	details(["smoke", "carbonMonoxide", "battery", "refresh"])
    }
}

// handle commands

def initialize() {
	log.info "Nest Protect ${textVersion()} ${textCopyright()}"
	poll()
}

def poll() {
    log.debug "Executing 'poll'"
    api('status', []) {
        data.topaz = it.data.topaz.getAt(settings.mac.toUpperCase())
        data.topaz.smoke_status = data.topaz.smoke_status == 0 ? 'clear' : 'detected'
        data.topaz.co_status = data.topaz.co_status == 0 ? 'clear' : 'detected'
        data.topaz.battery_health_state = data.topaz.battery_health_state  == 0 ? 'ok' : 'low'

        sendEvent(name: 'smoke', value: data.topaz.smoke_status, descriptionText: "${device.displayName} smoke ${data.topaz.smoke_status}!", displayed: false)
        sendEvent(name: 'carbonMonoxide', value: data.topaz.co_status, descriptionText: "${device.displayName} carbon monoxide ${data.topaz.co_status}!", displayed: false)
        sendEvent(name: 'battery', value: data.topaz.battery_health_state, descriptionText: "${device.displayName} battery is ${data.topaz.battery_health_state}!", displayed: false)
		if (data.topaz.smoke_status == "clear" && data.topaz.co_status == "clear"){
			sendEvent(name: 'alarmState', value: "clear")
        }
        else if (data.topaz.smoke_status == "detected"){
			sendEvent(name: 'alarmState', value: "smoke")        
        }
        else (data.topaz.co_status == "detected"){
			sendEvent(name: 'alarmState', value: "co")
        }
        log.debug "Smoke: ${data.topaz.smoke_status}"
        log.debug "CO: ${data.topaz.co_status}"
        log.debug "Battery: ${data.topaz.battery_health_state}"
    }
}

def api(method, args = [], success = {}) {
	if(!isLoggedIn()) {
		log.debug "Need to login"
		login(method, args, success)
		return
	}

	def methods = [
		'status': [uri: "/v2/mobile/${data.auth.user}", type: 'get'],
	]

	def request = methods.getAt(method)

	log.debug "Logged in"
	doRequest(request.uri, args, request.type, success)
}

// Need to be logged in before this is called. So don't call this. Call api.
def doRequest(uri, args, type, success) {
//	log.debug "Calling $type : $uri : $args"

	if(uri.charAt(0) == '/') {
		uri = "${data.auth.urls.transport_url}${uri}"
	}

	def params = [
		uri: uri,
		headers: [
			'X-nl-protocol-version': 1,
			'X-nl-user-id': data.auth.userid,
			'Authorization': "Basic ${data.auth.access_token}",
			'Accept-Language': 'en-us',
			'userAgent':  "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/600.8.9 (KHTML, like Gecko) Version/8.0.8 Safari/600.8.9"
		],
		body: args
	]

	def postRequest = { response ->
		if (response.getStatus() == 302) {
			def locations = response.getHeaders("Location")
			def location = locations[0].getValue()
			log.debug "redirecting to ${location}"
			doRequest(location, args, type, success)
		} else {
			success.call(response)
		}
	}

	try {
		if (type == 'get') {
			httpGet(params, postRequest)
		}
	} catch (Throwable e) {
		login()
	}
}

def login(method = null, args = [], success = {}) {
	def params = [
		uri: 'https://home.nest.com/user/login',
		body: [username: settings.username, password: settings.password]
	]

	httpPost(params) {response ->
		data.auth = response.data
		data.auth.expires_in = Date.parse('EEE, dd-MMM-yyyy HH:mm:ss z', response.data.expires_in).getTime()
		log.debug data.auth

		api(method, args, success)
	}
}

def isLoggedIn() {
	if(!data.auth) {
		log.debug "No data.auth"
		return false
	}

	def now = new Date().getTime();
	return data.auth.expires_in > now
}

private def textVersion() {
    def text = "Version 1.1"
}

private def textCopyright() {
    def text = "Copyright © 2014 Nick Bailey <nick@nickhbailey.com>"
}