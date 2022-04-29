package com.dolphin.saas.jobs;

import com.dolphin.saas.common.BaseTools;
import com.dolphin.saas.commons.RedisCommonUtils;
import com.dolphin.saas.commons.clouds.tencent.feature.GitLabServ;
import com.dolphin.saas.entity.Engineer;
import com.dolphin.saas.service.ClusterService;
import com.dolphin.saas.service.DeployService;
import com.dolphin.saas.service.EngineeringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

@Component
@Slf4j
public class ProjectSchedule extends BaseTools {
    @Autowired
    private RedisCommonUtils redisCommonUtils;

    @Resource
    private DeployService deployService;

    @Resource
    private ClusterService clusterService;

    @Resource
    private EngineeringService engineeringService;

    /**
     * 创建工程
     */
    @Scheduled(cron = "*/1 * * * * ?")
    public void createProjects() {
        try {
            ArrayList<Map<String, Object>> engineerList = engineeringService.FindEnginnerAllJobs();
            // 获取需要创建的工程
            for (Map<String, Object> items : engineerList) {
                try {
                    // 初始化幂等锁
                    String LdempootentLock = "createEngineer." + items.get("engineerId");
                    if (!redisCommonUtils.hasKeys(LdempootentLock)) {
                        // 设置幂等锁
                        if (!redisCommonUtils.noExpireSset(LdempootentLock, items.get("engineerId"))) {
                            throw new Exception("设置幂等锁失败!");
                        }

                        // 更新工程状态
                        engineeringService.updateJobsStatus(Long.parseLong(items.get("engineerId").toString()), 1);
                        // 初始化
                        GitLabServ gitLabServ = new GitLabServ(
                                "http://" + items.get("address").toString(),
                                items.get("username").toString(),
                                items.get("password").toString()
                        );

                        // 创建
                        Map<String, Object> gitRepo = gitLabServ.createProject(
                                items.get("engineerName").toString(),
                                items.get("engineerRemark").toString(),
                                Integer.parseInt(items.get("engineerGitGroupId").toString())
                        );

                        // 构建工程信息更新
                        Engineer engineer = new Engineer();
                        engineer.setEngineerGitId(Long.parseLong(gitRepo.get("id").toString()));
                        engineer.setEngineerId(Long.parseLong(items.get("engineerId").toString()));
                        engineer.setEngineerUpdatetime(new Date());
                        String gitLabUrl = gitRepo.get("url").toString().replace("http://0.0.0.0", items.get("address").toString());
                        if (!gitLabUrl.startsWith("http")) {
                            gitLabUrl = "http://" + gitLabUrl;
                        }
                        engineer.setEngineerGiturl(gitLabUrl);
                        engineeringService.UpdateJobsUrl(engineer);

                        // 更新工程状态
                        engineeringService.updateJobsStatus(Long.parseLong(items.get("engineerId").toString()), 2);
                        // 删除幂等锁
                        redisCommonUtils.getRedisTemplate().delete(LdempootentLock);
                    }
                } catch (Exception e) {
                    engineeringService.updateJobsStatus(Long.parseLong(items.get("engineerId").toString()), 3);
                    log.error("[GitLab][创建工程]子任务失败: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("[GitLab][创建工程]失败: {}", e.getMessage());
        }
    }
}
