package gitlab

import (
	"errors"
	"github.com/xanzy/go-gitlab"
	"log"
)

type jobInfo struct {
	Description string
	Name        string
	NamespaceID int
}

// gitlab登录
func GitLogin(username string, password string, baseUrl string) (*gitlab.Client, error) {
	git, err := gitlab.NewBasicAuthClient(
		username,
		password,
		gitlab.WithBaseURL(baseUrl),
	)
	if err != nil {
		log.Println("gitlab登录失败: "+err.Error(), "username:", username, "paasword:", password)
		return nil, err
	}
	return git, nil
}

// 创建工程
func CreateJobs(git *gitlab.Client, job jobInfo) (string, error) {
	// 先遍历所有项目
	projects, _, err := git.Projects.ListProjects(&gitlab.ListProjectsOptions{})
	if err != nil {
		return "", err
	}
	for _, projectItems := range projects {
		if projectItems.Name == job.Name {
			return "", errors.New("已经存在这个项目了,不用创建")
		}
	}

	// 创建项目
	p := &gitlab.CreateProjectOptions{
		Description: gitlab.String(job.Description),
		Name:        gitlab.String(job.Name),
		NamespaceID: gitlab.Int(job.NamespaceID),
	}
	project, _, err := git.Projects.CreateProject(p)
	if err != nil {
		return "", errors.New("创建项目失败:" + err.Error())
	}
	return project.WebURL, nil
}

// 拉取代码
