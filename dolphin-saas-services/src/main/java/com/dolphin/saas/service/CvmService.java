package com.dolphin.saas.service;

import com.dolphin.saas.entity.Cvm;
import com.dolphin.saas.entity.TagServ;
import com.dolphin.saas.entity.Tags;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface CvmService {
    // 获取cvm的数据列表 -- 前台API
    Map<String, Object> FindCvmLists(int Pages, int Size, String uuid);

    // 更新cvm数据(执行任务) -- 前台API
    void UpdateCvmJobs(String uuid) throws Exception;

    // 查看单条cvm数据 -- 前台API
    Map<String, Object> ReadAssetsCvm(int cid, String uuid);

    // 检测是否有更新cvm的任务 -- 前台API
    Boolean CheckCvmJobs(String uuid);

    // 打标签执行部署服务 -- 前台API
    Boolean BuildTagService(int cid, TagServ tagServ, String uuid);

    // 获取所有可以打的标签 -- 前台API
    ArrayList<Tags> FindTagLists();

    // 判断是否有Cvm资产在库里 -- 前台API
    Boolean checkCvmAlive(String uuid);

    // 获取所有的CVM的数据（底层工具） -- 工具API
    List<Cvm> FindCvmListForUp(String uuid);

    // 获取完CVM信息写入数据接口（底层工具） -- 工具API
    Map<String, Object> uploadCvm(Map<String, Object> cvmResults, String uuid);

    // 获取需要跑的任务列表（底层工具）-- 工具APi
    Map<String, Object> TaskLists();

    // 获取待部署的Tag任务列表（底层工具）
    Map<String, Object> DeployTagLists(Integer Pages, Integer Size);

    // 更新部署Tag任务的状态（底层工具）
    Map<String, Object> UpdateDeployTagStatus(Integer cid, Integer serviceId, Integer serviceStatus);
}
