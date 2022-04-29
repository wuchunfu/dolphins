package configur

import (
	"context"
	"errors"
	"github.com/bndr/gojenkins"
	"github.com/rafaeljesus/retry-go"
	"log"
	"os"
	"time"
)

// 在jenkins配置用户令牌
func ConfigCredentUsers() error {
	var envs = []string{"JENKINS_ADDRESS", "JENKINS_USERNAME", "JENKINS_PASSWORD"}
	for _, envsItems := range envs {
		if os.Getenv(envsItems) == "" {
			return errors.New("配置服务参数缺失初始化:" + envsItems)
		}
	}

	jenkinsUrl := os.Getenv("JENKINS_ADDRESS")
	jenkinsUser := os.Getenv("JENKINS_USERNAME")
	jenkinsPass := os.Getenv("JENKINS_PASSWORD")

	ctx := context.Background()
	jenkins := gojenkins.CreateJenkins(nil, jenkinsUrl, jenkinsUser, jenkinsPass)

	if err := retry.Do(func() error {
		// 配置基础服务
		_, err := jenkins.Init(ctx)
		if err != nil {
			log.Println("Jenkins初始化失败, 马上重试:" + err.Error())
			return err
		}
		return nil
	}, 5, time.Second*2); err != nil {
		return errors.New("jenkins初始化链接部分，重试了5次依然失败")
	}

	cm := gojenkins.CredentialsManager{J: jenkins}
	usernameCreds := []*gojenkins.UsernameCredentials{
		&gojenkins.UsernameCredentials{
			ID:          "gitlab",
			Description: "git拉取代码专用",
			Scope:       "GLOBAL",
			Username:    os.Getenv("GITLAB_USERNAME"),
			Password:    os.Getenv("GITLAB_PASSWORD"),
		},
		&gojenkins.UsernameCredentials{
			ID:          "k8sStore",
			Description: "拉取镜像专用",
			Scope:       "GLOBAL",
			Username:    os.Getenv("DOCKER_USERNAME"),
			Password:    os.Getenv("DOCKER_PASSWORD"),
		},
	}

	secretCreds := []*gojenkins.StringCredentials{
		&gojenkins.StringCredentials{
			ID:          "dolphins-sonar",
			Description: "sonar",
			Scope:       "GLOBAL",
			Secret:      os.Getenv("SONAR_TOKEN"),
		},
	}

	for _, cred := range usernameCreds {
		err := cm.Add(ctx, "_", cred)
		if err != nil {
			return errors.New("Jenkins创建用户密码失败:" + err.Error())
		}
	}

	for _, cred := range secretCreds {
		err := cm.Add(ctx, "_", cred)
		if err != nil {
			return errors.New("Jenkins创建用户秘钥失败:" + err.Error())
		}
	}
	return nil
}
