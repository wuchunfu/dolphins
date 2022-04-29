package com.dolphin.saas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dolphin.saas.common.BaseTools;
import com.dolphin.saas.entity.ServiceDeploy;
import com.dolphin.saas.entity.Vendors;
import com.dolphin.saas.entity.VendorsType;
import com.dolphin.saas.mapper.ClusterMapper;
import com.dolphin.saas.mapper.VendorsMapper;
import com.dolphin.saas.mapper.VendorsTypeMapper;
import com.dolphin.saas.service.VendorsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Service("vendorsService")
public class VendorsServiceimpl extends BaseTools implements VendorsService {

    @Resource
    private VendorsMapper vendorsMapper;

    @Resource
    private VendorsTypeMapper vendorsTypeMapper;

    @Resource
    private ClusterMapper clusterMapper;

    @Override
    public ArrayList<Map<String, Object>> VentorTypeOptions(String uuid, Boolean ventorCheck) throws Exception {
        ArrayList<Map<String, Object>> results = new ArrayList<>();

        try {
            // 查所有厂商
            QueryWrapper<VendorsType> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("t_status", 1);
            queryWrapper.select("t_name", "tid");
            List<VendorsType> vendorsTypeList = vendorsTypeMapper.selectList(queryWrapper);

            if (ventorCheck) {
                // 先查有哪些可用的厂商秘钥
                QueryWrapper<Vendors> queryWrapper2 = new QueryWrapper<>();
                queryWrapper2.eq("uuid", uuid);
                queryWrapper2.eq("v_status", 1);
                queryWrapper2.eq("v_defaults", 1);
                List<Vendors> vendorsList = vendorsMapper.selectList(queryWrapper2);

                ArrayList<Integer> UserVendors = new ArrayList<>();
                for (Vendors vendors : vendorsList) {
                    UserVendors.add(vendors.getTypeName());
                }

                if (UserVendors.size() < 1) {
                    throw new Exception("没有可用的厂商秘钥,请先添加!");
                }
                for (VendorsType vendorsType : vendorsTypeList) {
                    if (UserVendors.contains(vendorsType.getTid())) {
                        Map<String, Object> items = new HashMap<>();
                        items.put("label", vendorsType.getTName());
                        items.put("value", vendorsType.getTid());
                        // 如果有阿里云，则判断阿里云是否需要禁用
                        if (vendorsType.getTid() == 1){
                            QueryWrapper<ServiceDeploy> queryWrapper1 = new QueryWrapper<>();
                            queryWrapper1.eq("uuid", uuid);
                            queryWrapper1.eq("cluster_service_status", 3);
                            queryWrapper1.eq("cluster_delete", 1);
                            if (clusterMapper.selectCount(queryWrapper1) > 1){
                                items.put("disabled", 1);
                            }else{
                                items.put("disabled", 0);
                            }
                        }else{
                            items.put("disabled", 0);
                        }
                        results.add(items);
                    }
                }
            }else{
                for (VendorsType vendorsType : vendorsTypeList) {
                    Map<String, Object> items = new HashMap<>();
                    items.put("label", vendorsType.getTName());
                    items.put("value", vendorsType.getTid());
                    results.add(items);
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        return results;
    }

    @Override
    public void CreateCloudConf(Vendors vendors, String uuid) throws Exception {
        try {
            QueryWrapper<Vendors> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("type_name", vendors.getTypeName())
                    .eq("v_status", 1)
                    .eq("v_delete", 0)
                    .eq("uuid", uuid);

            vendors.setVid(null);
            if (vendorsMapper.selectCount(queryWrapper) < 1) {
                // 判断厂商是否有一个在用并设置为默认的秘钥，如果没有，则设置为默认
                vendors.setVdefaults(1);
            }else{
                vendors.setVdefaults(0);
            }

            // 到这状态都是1
            vendors.setStatus(1);
            // 查询这个云厂商的配置是否已经存在了
            queryWrapper.eq("v_access_key", vendors.getAccessKey())
                    .eq("v_access_secret", vendors.getAccessSecret())
                    .eq("uuid", uuid);
            if (vendorsMapper.selectCount(queryWrapper) > 0) {
                throw new Exception("密钥已有，无法创建!");
            } else {
                // 不存在就创建
                vendors.setCreateTime(new Date());
                vendors.setUuid(uuid);
                if (vendorsMapper.insert(vendors) < 1) {
                    throw new Exception("密钥创建失败,请联系客服!");
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> FindCloudLists(int Pages, int Size, String uuid) {
        // 获取所有的厂商分类
        Map<Integer, Object> vendorsObject = new HashMap<>();
        List<VendorsType> vendorsTypeList = vendorsTypeMapper.selectList(null);
        for (VendorsType vendorsType : vendorsTypeList) {
            vendorsObject.put(vendorsType.getTid(), vendorsType.getTName());
        }

        // 获取分页的数据
        IPage<Vendors> page = new Page<>(Pages, Size);
        QueryWrapper<Vendors> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("v_delete", 0);
        if (uuid != null) {
            queryWrapper.eq("uuid", uuid);
        }
        vendorsMapper.selectPage(page, queryWrapper);

        // 拼装分页数据
        Map<String, Object> results = new HashMap<>();
        results.put("page", Pages);
        results.put("total", page.getTotal());
        List<Vendors> vendorsList = page.getRecords();
        ArrayList<Map<String, Object>> recordsLists = new ArrayList<>();
        if (page.getTotal() > 0) {
            for (Vendors vendors : vendorsList) {
                Map<String, Object> records = this.objectMap(vendors);
                if (uuid != null) {
                    records.put("TypeName", vendorsObject.get(vendors.getTypeName()));
                }
                records.put("cloudId", vendors.getTypeName());
                records.remove("Vdelete");
                records.remove("uuid");
                recordsLists.add(records);
            }
        }
        results.put("list", recordsLists);
        results.put("pageSize", Size);
        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void UpdateCloudConf(Vendors vendors, String uuid) throws Exception {
        try {
            // 构建查询参数
            QueryWrapper<Vendors> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uuid", uuid);
            queryWrapper.eq("vid", vendors.getVid());

            // 判断uuid下，是否有这个密钥
            Vendors vendors1 = vendorsMapper.selectOne(queryWrapper);
            if (vendors1 == null) {
                throw new Exception("密钥不存在!");
            }

            // 判断下这个秘钥是不是重复的
            QueryWrapper<Vendors> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("v_access_key", vendors.getAccessKey());
            queryWrapper1.eq("v_access_secret", vendors.getAccessSecret());
            queryWrapper1.eq("uuid", uuid);
            Vendors vendors2 = vendorsMapper.selectOne(queryWrapper1);
            if (vendors2 != null){
                if (vendors2.getVid().equals(vendors.getVid())) {
                    throw new Exception("密钥没有变化，无需更新!");
                }else {
                    throw new Exception("密钥已经有了，不能重复!");
                }
            }

            // 判断下当前有没有这个厂商可用的秘钥，如果有，就不设置为默认，如果没有则设置为默认
            QueryWrapper<Vendors> queryWrapper2 = new QueryWrapper<>();
            queryWrapper2.eq("type_name", vendors1.getTypeName());
            queryWrapper2.eq("uuid", uuid);
            queryWrapper2.eq("v_status", 1);
            queryWrapper2.eq("v_defaults", 1);

            Vendors vendors3 = vendorsMapper.selectOne(queryWrapper2);
            if (vendors3 != null) {
                // 判断下是不是自己本身
                if (vendors3.getVid().equals(vendors1.getVid())){
                    vendors.setVdefaults(1);
                }else{
                    vendors.setVdefaults(0);
                }
            }else{
                // 设置为默认
                vendors.setVdefaults(1);
            }
            // 更新现在的密钥
            vendors.setUpdateTime(new Date());
            if (vendorsMapper.update(vendors, queryWrapper) < 1) {
                throw new Exception("更新密钥状态失败!");
            }

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Vendors ReadCloudConf(int vid, String uuid) {
        QueryWrapper<Vendors> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("vid", vid);
        queryWrapper.eq("uuid", uuid);
        return vendorsMapper.selectOne(queryWrapper);
    }

    @Override
    public Boolean CheckCloudConf(String uuid) {
        QueryWrapper<Vendors> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("v_status", 1);
        queryWrapper.eq("uuid", uuid);
        return vendorsMapper.selectCount(queryWrapper) >= 1;
    }

    @Override
    public void VentorDelete(Integer vid, String uuid) throws Exception {
        try {
            QueryWrapper<Vendors> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("vid", vid);
            queryWrapper.eq("uuid", uuid);
            if (vendorsMapper.selectCount(queryWrapper) < 1) {
                throw new Exception("没有这个密钥!");
            }
            Vendors vendors = new Vendors();
            vendors.setVdelete(1);
            if (vendorsMapper.update(vendors, queryWrapper) < 1) {
                throw new Exception("密钥删除失败!");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Map<String, String> FindUUidCloudKey(String uuid, Integer cloudId) throws Exception {
        Map<String, String> results = new HashMap<>();
        try {
            QueryWrapper<Vendors> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("v_defaults", 1);
            queryWrapper.eq("uuid", uuid);
            queryWrapper.eq("type_name", cloudId);
            Vendors vendors = vendorsMapper.selectOne(queryWrapper);
            if (vendors == null) {
                throw new Exception(String.format("无法获取对应的秘钥, UUID: %s, Type: %s", uuid, cloudId));
            }
            results.put("secreTld", vendors.getAccessKey());
            results.put("secreKey", vendors.getAccessSecret());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return results;
    }
}
