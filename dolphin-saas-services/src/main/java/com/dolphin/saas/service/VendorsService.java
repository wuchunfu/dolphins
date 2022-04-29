package com.dolphin.saas.service;

import com.dolphin.saas.entity.Vendors;

import java.util.ArrayList;
import java.util.Map;

public interface VendorsService {
    // 创建云厂商密钥配置 -- 前台API
    void CreateCloudConf(Vendors vendors, String uuid) throws Exception;

    // 云厂商密钥列表 -- 前台API
    Map<String, Object> FindCloudLists(int Page, int Size, String uuid);

    // 云厂商密钥更新 -- 前台API
    void UpdateCloudConf(Vendors vendors, String uuid) throws Exception;

    // 云厂商密钥读取 -- 前台API
    Vendors ReadCloudConf(int vid, String uuid);

    // 云厂商信息判断读取，判断有没有条目 -- 前台API
    Boolean CheckCloudConf(String uuid);

    // 逻辑删除云厂商 -- 前台API
    void VentorDelete(Integer vid, String uuid) throws Exception;

    // 获取云厂商列表 -- 前台API
    ArrayList<Map<String, Object>> VentorTypeOptions(String uuid, Boolean ventorCheck) throws Exception;

    // 根据uuid、云服务id，获取可用ak -- 工具API
    Map<String, String> FindUUidCloudKey(String uuid, Integer cloudId) throws Exception;
}
