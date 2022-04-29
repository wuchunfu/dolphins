package com.dolphin.saas.jobs;

import com.dolphin.saas.common.BaseTools;
import com.dolphin.saas.commons.RedisCommonUtils;
import com.dolphin.saas.commons.clouds.GitContor;
import com.dolphin.saas.service.EngineeringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Map;

@Component
@Slf4j
public class GitScheduler extends BaseTools {

    @Resource
    private EngineeringService engineeringService;

    @Resource
    private RedisCommonUtils redisCommonUtils;

    /**
     * 代码组成分析
     */
    @Scheduled(cron = "*/45 * * * * ?")
    public void analyzeFilelineProjects() {
        try {
            ArrayList<Map<String, Object>> engineerList = engineeringService.FindEngineerInfo();
            // 获取需要创建的工程
            for (Map<String, Object> items : engineerList) {
                // 初始化幂等锁
                String LdempootentLock = "analyzeFileline." + items.get("engineerId");
                try {
                    if (!redisCommonUtils.hasKeys(LdempootentLock)) {
                        // 设置幂等锁
                        redisCommonUtils.noExpireSset(LdempootentLock, items.get("engineerId"));
                        // 创建登录秘钥
                        GitContor gitContor = new GitContor(items.get("userName").toString(), items.get("passWord").toString());
                        // 获取线上commit
                        String onlineCommitId = gitContor.getRemoteCommitId(items.get("engineerGiturl").toString());

                        if (onlineCommitId == null){
                            // 如果获取不到就跳过
                            redisCommonUtils.getRedisTemplate().delete(LdempootentLock);
                            continue;
                        }

                        String commitId = null;
                        if (items.get("commitId") != null){
                            commitId = items.get("commitId").toString();
                            // 判断是否跟现在线上的版本一致
                            // 一致就跳过，不分析了
                            if (commitId.equals(onlineCommitId)) {
                                // 删除幂等锁
                                redisCommonUtils.getRedisTemplate().delete(LdempootentLock);
                                continue;
                            }
                        }

                        Map<String, Object> results = gitContor.cloneJobs(
                                items.get("engineerName").toString(),
                                items.get("engineerGiturl").toString());

                        // 保存更新数据
                        engineeringService.saveGitAnalyzeRecords(Long.parseLong(items.get("engineerId").toString()), results);

                        // 删除幂等锁
                        redisCommonUtils.getRedisTemplate().delete(LdempootentLock);
                    }
                } catch (Exception e) {
                    redisCommonUtils.getRedisTemplate().delete(LdempootentLock);
                }
            }
        } catch (Exception e) {
            log.error("[GitLab][代码组成分析]失败: {}", e.getMessage());
        }
    }
}
