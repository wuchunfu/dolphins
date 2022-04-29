package common

import (
	"io/ioutil"
	"os"
)

//创建并写入配置文件
func ConfigExists(path string, info string) error {
	_, err := os.Stat(path)
	if err != nil {
		// 不存在就创建下
		if os.IsNotExist(err) {
			// 创建文件夹
			err := os.MkdirAll(path, os.ModePerm)
			if err != nil {
				return err
			}
			// 写配置
			err = ioutil.WriteFile(path+"config", []byte(info), 0666)
			if err != nil {
				return err
			}
		}
	}
	return nil
}

// 创建写入yaml文件
func CreateYamlFile(path string, yamlInfo string, yamlName string) error {
	_, err := os.Stat(path)
	if err != nil {
		// 不存在就创建下
		if os.IsNotExist(err) {
			// 创建文件夹
			err := os.MkdirAll(path, os.ModePerm)
			if err != nil {
				return err
			}
		}
	}
	// 写配置
	err = ioutil.WriteFile(path+yamlName, []byte(yamlInfo), 0666)
	if err != nil {
		return err
	}
	return nil
}