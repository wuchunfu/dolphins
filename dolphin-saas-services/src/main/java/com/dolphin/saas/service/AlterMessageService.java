package com.dolphin.saas.service;

import java.util.Map;

public interface AlterMessageService {

    // 创建告警条目
    void createAlterMessage(String code, Integer type, String uuid) throws Exception;

    // 修改告警条目状态
    void changeAlterMessageStatus(Long id, Integer keyStatus, String uuid) throws Exception;

    // 删除告警提示条目
    void deleteAlterMessage(Long id, String uuid) throws Exception;

    // 告警提示条目列表
    Map<String, Object> AlterMessageLists(int Page, int Size, String uuid) throws Exception;
}
