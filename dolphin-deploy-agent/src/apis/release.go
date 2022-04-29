package apis

import (
	"errors"
	"io/ioutil"
	"log"
	"net/http"
	"net/url"
	"os"
)

//获取域名头
func getHosts() (host string, err error) {
	host = os.Getenv("DOLPHIN_API")
	if host == "" {
		return "", errors.New("开放平台API没有被配置")
	}
	return host, nil
}

//获取待发布列表
func GetReleaseLists(token string) (results string, err error) {
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

//获取集群基础配置
func GetClusterBase(apiPath string, clusterId string, token string) (results string, err error) {
	resp, err := http.PostForm("http://"+apiPath+"/release/clusterBase",
		url.Values{"token": {token}, "clusterId": {clusterId}},
	)
	if err != nil {
		return "", errors.New("获取集群基础信息异常")
	}
	defer resp.Body.Close()

	body, _ := ioutil.ReadAll(resp.Body)
	if resp.StatusCode == 200 {
		log.Println(string(body))
	}
	return string(body), nil
}
