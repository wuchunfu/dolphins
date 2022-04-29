package apis

import (
	"errors"
	"io/ioutil"
	"log"
	"net/http"
	"net/url"
)

//获取待创建工程列表
func GetGitlabLists(token string) (results string, err error) {
	host, err := getHosts()
	if err != nil {
		return "", err
	}
	resp, err := http.PostForm("http://"+host+"/release/taskLists",
		url.Values{"token": {token}},
	)
	if err != nil {
		return "", errors.New("获取发布列表异常")
	}
	defer resp.Body.Close()

	body, _ := ioutil.ReadAll(resp.Body)
	if resp.StatusCode == 200 {
		log.Println(string(body))
	}
	return string(body), nil
}
