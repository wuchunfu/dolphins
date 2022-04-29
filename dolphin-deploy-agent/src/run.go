package main

import (
	"dolphin-deploy-agent/src/common"
	"dolphin-deploy-agent/src/services/configur"
	"dolphin-deploy-agent/src/services/deploy"
	"embed"
	"flag"
	"github.com/rafaeljesus/retry-go"
	"log"
	"os"
	"time"
)

/**
	海豚工程自动化部署工具v3.0
 	- 增加了自动重试功能
	- 完善了整个服务套餐部分
	- 基于Golang把整个PYTHON版本重构了
 	- 部署和配置端全面整合了
*/

//go:embed yaml/*
var yamlBaseFiles embed.FS

func main() {
	// 判断激活的模块(0:部署端,1:配置端,2:发布端)
	var module int
	// 判断是否开启api模式
	var restful bool
	// 绑定参数
	flag.IntVar(&module, "P", 0, "模块选择,0:部署端,1:配置端,2:发布端，默认：0")
	flag.BoolVar(&restful, "A", false, "是否开启Resful API，默认：否")
	// 解析
	flag.Parse()
	log.Println("Dolphin v3.0 running...")

	if module == 0 {
		// 写入kubconf配置
		if err := retry.Do(func() error {
			err := common.ConfigExists("/root/.kube/", os.Getenv("KUBE_CONFIG"))
			if err != nil {
				log.Println("写入kubConf异常:", err.Error())
				return err
			}
			return nil
		}, 3, 1); err != nil {
			log.Println("重试3次失败,Kubconf无法配置!")
			return
		}

		if err := retry.Do(func() error {
			// 初始化Base配置
			err := deploy.Base(yamlBaseFiles)
			if err != nil {
				log.Println("初始化base内容异常:", err.Error())
				return err
			}
			return nil
		}, 5, time.Second); err != nil {
			log.Println("重试5次失败,Base无法配置!")
			return
		}

		// 判断当前的套餐
		buyMode := os.Getenv("BUY_MODE")
		if buyMode == "" {
			log.Println("套餐需要配置，当前无效")
			return
		}

		// 根据套餐执行
		switch buyMode {
		case "big":
			if err := retry.Do(func() error {
				// 初始化大型套餐配置
				err := deploy.Big(yamlBaseFiles)
				if err != nil {
					log.Println(err.Error())
					return err
				}
				return nil
			}, 5, time.Second); err != nil {
				log.Println("重试5次失败,大型套餐无法配置!")
				return
			}

		case "small":
			if err := retry.Do(func() error {
				// 初始化小型套餐配置
				err := deploy.Small(yamlBaseFiles)
				if err != nil {
					log.Println(err.Error())
					return err
				}
				return nil
			}, 5, time.Second); err != nil {
				log.Println("重试5次失败,小型套餐无法配置!")
				return
			}

		case "medium":
			if err := retry.Do(func() error {
				// 初始化中型套餐配置
				err := deploy.Medium(yamlBaseFiles)
				if err != nil {
					log.Println(err.Error())
					return err
				}
				return nil
			}, 5, time.Second); err != nil {
				log.Println("重试5次失败,中型套餐无法配置!")
				return
			}
		}
	}else if module == 1 {
		if err := retry.Do(func() error {
			// 配置基础服务
			err := configur.ConfigCredentUsers()
			if err != nil {
				log.Println(err.Error())
				return err
			}
			return nil
		}, 5, time.Second); err != nil {
			log.Println("重试5次失败,部署配置失败!")
			return
		}
	}

	log.Println("Dolphin v3.0 finish!")
}
