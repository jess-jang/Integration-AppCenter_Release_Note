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
 **/
def result = reqChangeCommit()()
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
 * @return Release Note Template 리턴
 */
def reqChangeCommit() {
	println "reqChangeCommit"
  
	try {
      
        def apiUrl = getJenkinsUrl()
        println apiUrl
		def response = reqApi(getJenkinsAuth(), apiUrl)

		// json으로 변환
		def jsonSlurper = new JsonSlurper()
		def json = jsonSlurper.parseText(response.toString()) 

		// Map
		def noteMap = new HashMap<String, String>()
    
      	// 커밋 정보 파싱
		def items = json.changeSet.items.reverse() // 최신 정보가 0번째 올라오게 소팅 
		for(item in items) {
            def format = getJiraId(item) // 커밋 메세지 정보
            if(format == null) {
                continue
            }

            def ids = format.split(",")
            println "comment ${format}"
            // println "format ${format}"
            // println "ids ${ids}" 	
            if (task.contains("NOTE")) {
                putMapStringNote(noteMap, comment)
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
        temaplte += noteEtc
        
        /**
         * Result
         **/
      	return temaplte
                           
  	} catch(Exception e) {
        "Exception reqChangeCommit"
		println e.getMessage()
	}
}

/**
 * Map 에 데이터 삽입 Note
 */
def putMapStringNote(map, comment) {
    def data = comment.replaceAll("\\[NOTE\\]","").replaceAll("\\[note\\]","").trim()
  	def isExist = map.get(data)
  	if(!isExist || isExist == null) {
    	map.put(data, true)
  	}	
}

/**
 * Map 형태의 데이터를 \n \n 로 리턴
 * @parma isTask 테명 노출여부
 */
def getNoteContent(isNote, map) {
    def list = []
    for (entry in map) {
      if(isNote) {
        list.add(entry.key)
      } else {
        list.add(entry.value)
      }
  	}
  	// 정제된 메세지 
  	def result = list.join("\n")
	println result

    if(result.length() > 0) {
	  	return result
    } else {
	  	return null
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