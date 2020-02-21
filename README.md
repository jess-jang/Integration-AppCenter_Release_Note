## Release Note 자동화
- 커밋 메세지의 `[TASK_ID]`를 추출하여 Release Note 자동화

## 방법
- Jenkins API > Git Diff List 
- Commit Message의 Jira ID 파싱
- 커밋 메세지
`[JIRA_ID] 커밋내용`
![git](https://github.com/jess-jang/Integration-Jira_Comment/blob/master/screenshot_git.png?raw=true "git")

## APIs
### Jenkins
마지막빌드 상태 조회 : `https://{JENKINS}/jenkins/{JOB_NAME}/lastBuild/api/json`
> https://wiki.jenkins.io/display/JENKINS/Remote+access+API
