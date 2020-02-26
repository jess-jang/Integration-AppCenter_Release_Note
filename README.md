## Release Note 자동화
- 커밋 메세지의 `[TASK_ID]`와 `Message (Subject)` 추출하여 Release Note 자동화

```
// Example 
빌드번호
134

수정사항
검색탭 리프레시 이슈수정
마이페이지 아이콘 변경 

QA
2020, 2424, 3837, 4858

UXD
393, 595, 696
```


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
