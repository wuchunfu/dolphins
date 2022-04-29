package com.dolphin.saas.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dolphin.saas.common.BaseTools;
import com.dolphin.saas.common.RedisUtilServ;
import com.dolphin.saas.entity.*;
import com.dolphin.saas.mapper.*;
import com.dolphin.saas.service.CvmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Service("cvmService")
public class CvmServiceimpl extends BaseTools implements CvmService {

    @Resource
    private CvmMapper cvmMapper;

    @Resource
    private TagServMapper tagServMapper;

    @Resource
    private TagMapper tagMapper;

    @Resource
    private VendorsTypeMapper vendorsTypeMapper;

    @Resource
    private VendorsMapper vendorsMapper;

    @Autowired
    private RedisUtilServ redisUtils;

    /**
     * 获取所有的服务,以kv的方式保存
     *
     * @return map
     */
    protected Map<String, Object> getServices() {
        List<Tags> tagsLists = tagMapper.selectList(null);
        Map<String, Object> tagsMap = new HashMap<>();
        for (Tags tags : tagsLists) {
            tagsMap.put(tags.getTagId().toString(), tags.getTagName());
        }
        return tagsMap;
    }

    /**
     * 获取云厂商列表
     *
     * @return map数据字典
     */
    protected Map<String, Object> getVentors() {
        List<VendorsType> vendorsTypesLists = vendorsTypeMapper.selectList(null);
        Map<String, Object> vendorsTypeMap = new HashMap<>();
        for (VendorsType vendorsType : vendorsTypesLists) {
            vendorsTypeMap.put(vendorsType.getTid().toString(), vendorsType.getTName());
        }
        return vendorsTypeMap;
    }

    @Override
    public Map<String, Object> FindCvmLists(int Pages, int Size, String uuid) {
        Map<String, Object> tagsMap = this.getServices();
        Map<String, Object> vendorsTypeMap = this.getVentors();
        // 获取分页的数据
        IPage<Cvm> page = new Page<>(Pages, Size);
        QueryWrapper<Cvm> queryWrapper = new QueryWrapper<>();
        if (uuid != null) {
            queryWrapper.eq("uuid", uuid);
        }
        // 没有被逻辑删除
        queryWrapper.eq("cvm_delete", 0);
        cvmMapper.selectPage(page, queryWrapper);
        // 获取原始数据
        List<Cvm> cvmAssets = page.getRecords();
        // 结果数据
        ArrayList results = new ArrayList();
        // 处理数据
        for (Cvm cvmAsset : cvmAssets) {
            Map<String, Object> newCvmAssets = this.objectMap(cvmAsset);
            // 根据id获取云厂商
            newCvmAssets.put("regionSource", vendorsTypeMap.get(cvmAsset.getCvmRegionSource().toString()));
            // 查对应的服务id
            QueryWrapper<TagServ> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("cid", cvmAsset.getCid());
            queryWrapper1.select("tag_id");
            queryWrapper1.eq("service_status", 1);
            List<TagServ> tagService = tagServMapper.selectList(queryWrapper1);
            ArrayList tagServiceLists = new ArrayList();
            // 找对应的服务名
            for (TagServ tagServ : tagService) {
                tagServiceLists.add(tagsMap.get(tagServ.getTagId().toString()).toString());
            }
            newCvmAssets.put("serviceLabel", tagServiceLists);
            results.add(newCvmAssets);
        }

        // 拼装分页数据
        Map<String, Object> response = new HashMap<>();
        response.put("page", Pages);
        response.put("total", page.getTotal());
        response.put("list", results);
        response.put("pageSize", Size);
        return response;
    }

    @Override
    public void UpdateCvmJobs(String uuid) throws Exception {
        try {
            QueryWrapper<Vendors> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uuid", uuid);
            // 先判断有没有ak
            if (vendorsMapper.selectCount(queryWrapper) < 1) {
                throw new Exception("请先创建密钥!");
            }
            // 判断是否有任务了
            if (redisUtils.hasKeys("reload.cvm." + uuid + ".jobs")) {
                throw new Exception("不能反复创建任务，请等待任务完成!");
            }
            // 更新cvm信息的任务创建
            redisUtils.noExpireSset("reload.cvm." + uuid + ".jobs", 1);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> ReadAssetsCvm(int cid, String uuid) {
        Map<String, Object> tagsMap = this.getServices();
        Map<String, Object> newCvmAssets = this.objectMap(cvmMapper.selectById(cid));

        // 查归属的云
        QueryWrapper<VendorsType> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tid", newCvmAssets.get("cvmRegionSource"));
        VendorsType vendorsType = vendorsTypeMapper.selectOne(queryWrapper);
        if (vendorsType != null) {
            newCvmAssets.put("cvmRegionSource", vendorsType.getTName());
        }

        // 先查这个cid跟uuid是否匹配
        QueryWrapper<Cvm> cvmQueryWrapper = new QueryWrapper<>();
        cvmQueryWrapper.eq("uuid", uuid);
        cvmQueryWrapper.eq("cid", cid);

        if (cvmMapper.selectCount(cvmQueryWrapper) < 1) {
            return new HashMap<>();
        }

        // 查对应的服务id
        QueryWrapper<TagServ> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("cid", cid);
        queryWrapper1.select("tag_id", "service_port", "service_status", "service_createtime", "service_id");
        queryWrapper1.eq("service_status", 1);
        List<TagServ> tagService = tagServMapper.selectList(queryWrapper1);
        ArrayList tagServiceLists = new ArrayList<>();
        Map<String, Object> serviceLists = new HashMap<>();
        // 找对应的服务名
        for (TagServ tagServ : tagService) {
            String serviceName = tagsMap.get(tagServ.getTagId().toString()).toString();
            tagServiceLists.add(serviceName);
            // 组合service的列表
            serviceLists.put("serviceId", tagServ.getServiceId());
            serviceLists.put("serviceName", serviceName);
            serviceLists.put("createTime", tagServ.getServiceCreatetime());
            serviceLists.put("port", tagServ.getServicePort());
            serviceLists.put("status", tagServ.getServiceStatus());
        }
        newCvmAssets.put("cvmTag", tagServiceLists);
        newCvmAssets.put("serviceLists", serviceLists);
        return newCvmAssets;
    }

    @Override
    public Boolean CheckCvmJobs(String uuid) {
        return redisUtils.hasKeys("reload.cvm." + uuid + ".jobs");
    }

    @Override
    public Boolean BuildTagService(int cid, TagServ tagServ, String uuid) {
        // 先查这个cid跟uuid是否匹配
        QueryWrapper<Cvm> cvmQueryWrapper = new QueryWrapper<>();
        cvmQueryWrapper.eq("uuid", uuid);
        cvmQueryWrapper.eq("cid", cid);

        if (cvmMapper.selectCount(cvmQueryWrapper) < 1) {
            return false;
        }
        // 先查有没有同类标签，不能重复部署
        QueryWrapper<TagServ> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tag_id", tagServ.getTagId())
                .eq("cid", cid);
        // 判断资产是否存在这个服务,存在就不能新建了
        if (tagServMapper.selectCount(queryWrapper) > 0) {
            return false;
        }
        // 不存在，则创建服务
        tagServ.setCid(cid);
        tagServ.setServiceCreatetime(new Date());
        return tagServMapper.insert(tagServ) > 0;
    }

    @Override
    public ArrayList<Tags> FindTagLists() {
        // 只获取没有被逻辑删除的部分
        QueryWrapper<Tags> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tag_delete", 0);
        queryWrapper.select("tagid", "tag_name");
        List<Tags> tagsList = tagMapper.selectList(queryWrapper);
        ArrayList results = new ArrayList();
        for (Tags tags : tagsList) {
            Map<String, Object> items = new HashMap<>();
            items.put("label", tags.getTagName());
            items.put("value", tags.getTagId());
            results.add(items);
        }
        return results;
    }

    @Override
    public Boolean checkCvmAlive(String uuid) {
        // 查目前是否有资产存在
        QueryWrapper<Cvm> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uuid", uuid);
        return cvmMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public List<Cvm> FindCvmListForUp(String uuid) {
        QueryWrapper<Cvm> cvmQueryWrapper = new QueryWrapper<>();
        cvmQueryWrapper.select(
                "cvm_instance_id", "cvm_config",
                "cvm_region_source", "cvm_status",
                "cvm_createtime", "cvm_tag_name",
                "cvm_region_id", "cvm_cluster_inside_ip",
                "cvm_cluster_outside_ip", "cvm_cost"
        );
        cvmQueryWrapper.eq("uuid", uuid);
        cvmQueryWrapper.eq("cvm_delete", 0);
        return cvmMapper.selectList(cvmQueryWrapper);
    }

    @Override
    public Map<String, Object> uploadCvm(Map<String, Object> cvmResults, String uuid) {
        Map<String, Object> results = new HashMap<>();
        try {
            if (cvmResults.containsKey("delete")) {
                for (String cvmItems : (List<String>) cvmResults.get("delete")) {
                    try {
                        Cvm hashMap = JSON.parseObject(cvmItems, Cvm.class);
                        QueryWrapper<Cvm> queryWrapper = new QueryWrapper<>();
                        queryWrapper.eq("cvm_instance_id", hashMap.getCvmInstanceId());
                        queryWrapper.eq("uuid", uuid);
                        hashMap.setUuid(uuid);
                        hashMap.setCvmUpdateTime(new Date());
                        hashMap.setCvmDelete(1);
                        if (cvmMapper.update(hashMap, queryWrapper) < 1) {
                            throw new Exception("更新CVM数据失败:" + hashMap.getCvmInstanceId());
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        throw new Exception(e.getMessage());
                    }
                }
            }
            if (cvmResults.containsKey("add")) {
                for (String cvmItems : (List<String>) cvmResults.get("add")) {
                    try {
                        Cvm hashMap = JSON.parseObject(cvmItems, Cvm.class);
                        QueryWrapper<Cvm> queryWrapper = new QueryWrapper<>();
                        queryWrapper.eq("cvm_instance_id", hashMap.getCvmInstanceId());
                        queryWrapper.eq("uuid", uuid);
                        if (cvmMapper.selectCount(queryWrapper) > 0) {
                            hashMap.setCvmUpdateTime(new Date());
                            hashMap.setUuid(uuid);
                            // 把逻辑删除清理掉
                            hashMap.setCvmDelete(0);
                            if (cvmMapper.update(hashMap, queryWrapper) < 1) {
                                throw new Exception("更新失败!");
                            }
                        } else {
                            hashMap.setUuid(uuid);
                            if (cvmMapper.insert(hashMap) < 1) {
                                throw new Exception("插入失败!");
                            }
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        throw new Exception(e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            results.put("error", e.getMessage());
        }
        return results;
    }

    @Override
    public Map<String, Object> TaskLists() {
        Set<String> keys = redisUtils.getRedisTemplate().keys("reload.cvm*");
        Map<String, Object> map = new HashMap<>();
        for (String key : keys) {
            Object val = redisUtils.getRedisTemplate().opsForValue().get(key);
            map.put(key, val);
        }
        return map;
    }

    @Override
    public Map<String, Object> DeployTagLists(Integer Pages, Integer Size) {
        return null;
    }

    @Override
    public Map<String, Object> UpdateDeployTagStatus(Integer cid, Integer serviceId, Integer serviceStatus) {
        return null;
    }
}