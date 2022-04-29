package deploy

import (
	"dolphin-deploy-agent/src/common"
	"embed"
	"errors"
	"log"
	"os"
	"os/exec"
	"regexp"
	"strings"
)

// 基础服务的部分
func Base(yamlBaseFiles embed.FS) error {
	var cloud = os.Getenv("CLOUD")
	var aliyunEnvs = []string{"SC_NAME", "REGION_ID", "ZONE_ID"}
	var tencentEnvs = []string{"SC_NAME", "PGROUP_ID", "SUBNET_ID", "VPC_ID", "ZONE_ID"}

	// 先判断是否云参数缺失,这个用于判断环境
	if cloud == "" {
		return errors.New("缺失云厂商配置参数")
	}

	var tencentData = ""
	// 根据云判断用哪个服务
	switch cloud {
	case "ALIYUN":
		// 获取下阿里云下的主要参数
		for _, aliyunEnvsItems := range aliyunEnvs {
			if os.Getenv(aliyunEnvsItems) == "" {
				return errors.New("阿里云配置缺失初始化: " + aliyunEnvsItems)
			}
		}
		// 初始化读取Yaml
		yamlData, err := yamlBaseFiles.ReadFile("yaml/base/00-aliyun.yaml")
		if err != nil {
			panic(err)
		}
		// 替换内容
		keywordsLists := strings.NewReplacer(
			"<SC_NAME>", os.Getenv("SC_NAME"),
			"<REGION_ID>", os.Getenv("REGION_ID"),
			"<ZONE_ID>", os.Getenv("ZONE_ID"))
		tencentData = keywordsLists.Replace(string(yamlData))

	case "TENCENT":
		// 获取下腾讯云下的主要参数
		for _, tencentEnvsItems := range tencentEnvs {
			if os.Getenv(tencentEnvsItems) == "" {
				return errors.New("腾讯云配置缺失初始化: " + tencentEnvsItems)
			}
		}
		// 初始读取Yaml
		yamlData, err := yamlBaseFiles.ReadFile("yaml/base/00-tencent.yaml")
		if err != nil {
			panic(err)
		}

		// 替换内容
		keywordsLists := strings.NewReplacer(
			"<SC_NAME>", os.Getenv("SC_NAME"),
			"<PGROUP_ID>", os.Getenv("PGROUP_ID"),
			"<SUBNET_ID>", os.Getenv("SUBNET_ID"),
			"<VPC_ID>", os.Getenv("VPC_ID"),
			"<ZONE_ID>", os.Getenv("ZONE_ID"))
		tencentData = keywordsLists.Replace(string(yamlData))
	}

	// 写入到Yaml文件
	err := common.CreateYamlFile("base/", tencentData, "00-base.yaml")
	if err != nil {
		return errors.New("配置初始化base失败: " + err.Error())
	}

	// 初始化namespace的内容配置
	yamlDefalutByte, err := yamlBaseFiles.ReadFile("yaml/base/01-default.yaml")
	if err != nil {
		return errors.New("配置初始化base的namespace失败")
	}

	yamlDefalutData := strings.Replace(string(yamlDefalutByte), "<SC_NAME>", os.Getenv("SC_NAME"), -1)
	err = common.CreateYamlFile("base/", yamlDefalutData, "01-default.yaml")
	if err != nil {
		return errors.New("写入配置base的namespace配置失败")
	}

	// 执行外部命令
	cmd := exec.Command("kubectl", "create", "-f", "/base")
	output, err := cmd.CombinedOutput()
	if err != nil {
		log.Println("异常信息:" + string(output))
		return errors.New("执行初始化配置失败:" + err.Error())
	}

	// 正则匹配是否正常
	var errorMessage = []string{"timeout", "error", "socket", "unknown", "lookup"}
	for _, errorItems := range errorMessage {
		matchBool, err := regexp.Match(errorItems+"*", output)
		if err != nil {
			return errors.New("执行初始化异常")
		}

		if matchBool {
			return errors.New("部署初始化失败")
		}
	}
	return nil
}
