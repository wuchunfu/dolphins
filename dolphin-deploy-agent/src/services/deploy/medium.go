package deploy

import (
	"dolphin-deploy-agent/src/common"
	"embed"
	"errors"
	"os"
	"os/exec"
	"regexp"
	"strings"
)

// 中型套餐
func Medium(yamlBaseFiles embed.FS) error {
	var envs = []string{
		"SC_NAME", "MYSQL_ROOT_PASSWORD", "REDIS_PASSWORD", "GLOBAL_PASSWORD", "YAPI_DB_PASSWORD", "K8S_ADDR", "GITLAB_ADDRESS",
		"HFISH_ADDRESS", "JUMPSERVER_ADDRESS", "METERSPHERE_ADDRESS", "SKYWALKING_ADDRESS", "SONAR_ADDRESS",
		"YAPI_ADDRESS", "NEXUS_ADDRESS", "DOCKER_ADDRESS", "JENKINS_ADDRESS", "SENTRY_ADDRESS", "CLUSTER_ID", "CLUSTER_INSTANCE_ID",
		"DOCKER_HUB", "DOCKER_USERNAME", "DOCKER_PASSWORD",
	}

	err := common.CheckEnv(envs)
	if err != nil {
		return err
	}

	var cloud = os.Getenv("CLOUD")
	// 获取所有的yaml
	fileDir, err := yamlBaseFiles.ReadDir("yaml/small")
	if err != nil {
		return err
	}

	// 遍历
	for _, fileItems := range fileDir {
		switch cloud {
		case "ALIYUN":
			if strings.Contains("03-ingress-controller.yaml", fileItems.Name()) {
				continue
			}

			if strings.Contains("20-tencent-ingress.yaml", fileItems.Name()) {
				continue
			}

		case "TENCENT":
			if strings.Contains("20-aliyun-ingress.yaml", fileItems.Name()) {
				continue
			}
		}

		// 读取yaml并替换内容
		yamlData, err := yamlBaseFiles.ReadFile("yaml/medium/" + fileItems.Name())
		if err != nil {
			return err
		}
		keywordsLists := strings.NewReplacer(
			"<SC_NAME>", os.Getenv("SC_NAME"),
			"<MYSQL_ROOT_PASSWORD>", os.Getenv("MYSQL_ROOT_PASSWORD"), "<REDIS_PASSWORD>", os.Getenv("REDIS_PASSWORD"),
			"<GLOBAL_PASSWORD>", os.Getenv("GLOBAL_PASSWORD"), "<YAPI_DB_PASSWORD>", os.Getenv("YAPI_DB_PASSWORD"),
			"<K8S_ADDR>", os.Getenv("K8S_ADDR"), "<GITLAB_ADDRESS>", os.Getenv("GITLAB_ADDRESS"),
			"<HFISH_ADDRESS>", os.Getenv("HFISH_ADDRESS"), "<JUMPSERVER_ADDRESS>", os.Getenv("JUMPSERVER_ADDRESS"),
			"<METERSPHERE_ADDRESS>", os.Getenv("METERSPHERE_ADDRESS"), "<SKYWALKING_ADDRESS>", os.Getenv("SKYWALKING_ADDRESS"),
			"<SONAR_ADDRESS>", os.Getenv("SONAR_ADDRESS"), "<YAPI_ADDRESS>", os.Getenv("YAPI_ADDRESS"),
			"<NEXUS_ADDRESS>", os.Getenv("NEXUS_ADDRESS"), "<DOCKER_ADDRESS>", os.Getenv("DOCKER_ADDRESS"),
			"<JENKINS_ADDRESS>", os.Getenv("JENKINS_ADDRESS"), "<SENTRY_ADDRESS>", os.Getenv("SENTRY_ADDRESS"), "<DOLPHIN_API>", "open.aidolphins.com",
			"<CLUSTER_ID>", os.Getenv("CLUSTER_ID"), "<CLUSTER_INSTANCE_ID>", os.Getenv("CLUSTER_INSTANCE_ID"), "<CONSUMER_COUNT>", "10",
			"<CONSUMER_SLEEP>", "5", "<PRODUCER_SLEEP>", "5", "<REDIS_POOL_HOST>", "redis", "<REDIS_POOL_PORT>", "6379", "<REDIS_POOL_DB>", "4",
			"<REDIS_POOL_MAX_CONNECT>", "20", "<SONAR_ADDR>", os.Getenv("SONAR_ADDRESS"),
			"<SONAR_USERNAME>", "admin", "<SONAR_PASSWORD>", os.Getenv("GLOBAL_PASSWORD"),
			"<SONAR_SPIDER_POOL>", "10", "<IMAGE_TASK_POOL>", "20", "<DOCKER_HUB>", os.Getenv("DOCKER_HUB"),
			"<DOCKER_USERNAME>", os.Getenv("DOCKER_USERNAME"), "<DOCKER_PASSWORD>", os.Getenv("DOCKER_PASSWORD"))
		tencentData := keywordsLists.Replace(string(yamlData))

		// 写入到Yaml文件
		err = common.CreateYamlFile("medium/", tencentData, fileItems.Name())
		if err != nil {
			return errors.New("组建小型套餐配置失败")
		}
	}

	// 构建执行小型套餐
	cmd := exec.Command("kubectl", "create", "-f", "/medium/")
	output, err := cmd.CombinedOutput()
	if err != nil {
		return errors.New("执行初始化配置失败")
	}

	// 正则匹配是否正常
	var errorMessage = []string{"timeout", "error", "socket", "unknown", "lookup"}
	for _, errorItems := range errorMessage {
		matchBool, err := regexp.Match(errorItems+"*", []byte(string(output)))
		if err != nil {
			return errors.New("执行初始化异常")
		}

		if matchBool {
			return errors.New("部署初始化失败")
		}
	}
	return nil
}
