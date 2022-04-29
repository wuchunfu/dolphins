package apis

import (
	"errors"
	"github.com/Andrew-M-C/go.jsonvalue"
	"io/ioutil"
	"net/http"
	"net/url"
)

//跟开放平台集成登录
func login(AccessKey string, AccessSecret string) (response *jsonvalue.V, err error) {
	host, err := getHosts()
	if err != nil {
		return &jsonvalue.V{}, err
	}
	resp, err := http.PostForm("http://"+host+"/auth/login",
		url.Values{"accessKey": {AccessKey}, "accessSecret": {AccessSecret}},
	)
	if err != nil {
		return &jsonvalue.V{}, errors.New("登录失败")
	}
	defer resp.Body.Close()

	body, _ := ioutil.ReadAll(resp.Body)
	if resp.StatusCode == 200 {
		// 转换成json
		response, err := jsonvalue.Unmarshal(body)
		if err != nil {
			return &jsonvalue.V{}, errors.New("解析结果异常")
		}
		return response, nil
	}
	return nil, errors.New("请求不正常,状态码:" + string(resp.StatusCode) + "返回内容:" + string(body))
}
