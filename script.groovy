import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.URL
import java.net.URLConnection
import java.net.*
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import hudson.model.*;
import hudson.util.*;
import hudson.scm.*;
import hudson.plugins.accurev.*
import java.util.*

println "ITEM_BUILD_NUMBER : ${System.getenv("ITEM_BUILD_NUMBER")}"
println "ITEM_JOB_NAME : ${System.getenv("ITEM_JOB_NAME")}"
println "JENKINS_AUTHORIZATION : ${System.getenv("JENKINS_AUTHORIZATION")}"


/**
 * 시작
 */
def result = reqChangeCommit()

/**
 * 
 */
println temaplte

/** 
 * 빌드넘버
 */
def getBuildNumber() {
	return System.getenv("ITEM_BUILD_NUMBER")
}

/**
 * Jenkin Job URL
 **/
def getJenkinsUrl() {
	def url = "%sjob/%s/lastBuild/api/json"
	url = String.format(url, System.getenv("JENKINS_URL"), URLEncoder.encode(System.getenv("ITEM_JOB_NAME"), "UTF-8"))
	println "JENKIN_URL : ${url}" 
	return url
}

/**
 * Jenkins Authentication
 **/
def getJenkinsAuth() {
	// 1. Jenkins > Profile
	// 2. API Token > ADD NEW TOKEN
	def jenkinsAuth = System.getenv("JENKINS_AUTHORIZATION")
	return "${jenkinsAuth}".bytes.encodeBase64().toString()
}

/**
 * 커밋 메세지 조회
 */
def reqChangeCommit() {
	try {
		def apiUrl = getJenkinsUrl()
		def response = reqApi(getJenkinsAuth(), apiUrl)
		return getTemaplte(response)							
  	} catch(Exception e) {
		println e.getMessage()
	}
}

def getTemaplte(response) {
	try {
		// json으로 변환
		def json = new JsonSlurper().parseText(response.toString()) 

		// Map
		def noteMap = new HashMap<String, Boolean>()
		def qaMap = new HashMap<String, Boolean>()
		def etcMap = new HashMap<String, Boolean>()
    
		// 커밋 정보 파싱
		def items = json.changeSet.items.reverse() // 최신 정보가 0번째 올라오게 소팅 
		for(item in items) {
			def id = getJiraId(item) // 커밋 메세지 정보
			if(id == null) {
				continue
			}

			if (task.contains("NOTE")) {
				// [NOTE]
				putMapStringNote(noteMap, comment)
			} else if (task.contains("QA")) {
				// [QA]
				putMapBoolean(qaMap, comment)
			} else {
				// [...] 
				putMapBoolean(etcMap, comment)
			}	
		} 

		def noteContent = getNoteContent(true, noteMap)
		println "noteContent : ${noteContent}"

		def temaplte = ""
		temaplte += "** 빌드번호 **"      
		temaplte += "\n\n"
		temaplte += getBuildNumber()
		temaplte += "\n\n"
		temaplte += "** 수정사항 **"
		temaplte += "\n\n"      
		temaplte += getMapValue(true, noteMap)
		temaplte += getMapValue(true, etcMap)
		temaplte += "\n\n"
		temaplte += "** QA **"
		temaplte += "\n\n"      
		temaplte += getMapValue(true, qaMap)
        
		/**
		 * Result
		 **/
      		return temaplte      
  	} catch(Exception e) {
		println e.getMessage()
	}	
}

/**
 * Map에 데이터 삽입 Note
 */
def putMapStringNote(map, comment) {
	def data = comment.replaceAll("\\[NOTE\\]","").trim()
	def isExist = map.get(data)
	if(!isExist) {
    		map.put(data, true)
  	}	
}

/**
 * Map에 데이터 삽입 Boolean
 */
def putMapBoolean(map, comment) {
	def isExist = map.get(comment)
	if(!isExist) {
    		map.put(data, true)
  	}	
}

/**
 * Map 형태의 데이터를 \n \n 로 리턴
 */
def getMapValue(map) {
	def list = []
	for (entry in map) {
		list.add(entry.value)
  	}
  	
	// 정제된 메세지 
  	def result = list.join("\n\n")
	if(result.length() > 0) {
		return result
	} else {
		return "-"
	}
}

/**
 * Commit 메세지의 "[]" 안의 내용 추출 
 */
def getJiraId(msg) {
	try {
		def matches = msg =~ "^\\[.*?\\]"
		return matches[0].replaceAll("\\[","").replaceAll("\\]","")  
	} catch(Exception e) {
        	""
	}
	return null
}

/**
 * API 통신
 */
def reqApi(auth, apiUrl, data = "") {
  	try {
		// HTTP Request
		def url = new URL(apiUrl)
		def conn = url.openConnection()
		conn.setDoOutput(true)
		conn.setRequestMethod('POST')
		conn.setRequestProperty("Accept", 'application/json')
		conn.setRequestProperty("Content-Type", 'application/json')  	
		conn.setRequestProperty("Authorization", "Basic ${auth}")

		def writer = new OutputStreamWriter(conn.getOutputStream())

		if(data != "") {
		    writer.write(data)
		    writer.flush()
		}

		def reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))
		def response = reader.readLine()
		println response

		writer.close()
		reader.close()  
		return response
	} catch(Exception e) {
		println e.getMessage()
	}
	return ""
}
