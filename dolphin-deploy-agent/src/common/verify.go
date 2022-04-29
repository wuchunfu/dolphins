package common

import (
	"errors"
	"os"
)

// 判断对应的参数是否都设置了
func CheckEnv(envs []string) error {
	// 确认所有参数都存在
	for _, envsItems := range envs {
		if os.Getenv(envsItems) == "" {
			return errors.New("配置参数缺失初始化: " + envsItems)
		}
	}
	return nil
}
