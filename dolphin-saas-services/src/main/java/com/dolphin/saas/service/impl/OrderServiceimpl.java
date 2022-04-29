package com.dolphin.saas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dolphin.saas.entity.EngineerDockerfile;
import com.dolphin.saas.entity.Orders;
import com.dolphin.saas.entity.ServiceDeploy;
import com.dolphin.saas.entity.vo.DockerLists;
import com.dolphin.saas.entity.vo.OrderLists;
import com.dolphin.saas.mapper.ClusterMapper;
import com.dolphin.saas.mapper.OrdersMapper;
import com.dolphin.saas.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service("orderService")
public class OrderServiceimpl implements OrderService {
    @Resource
    private OrdersMapper ordersMapper;

    @Resource
    private ClusterMapper clusterMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Orders CreateOrder(String commonId, Integer sourceId, String uuid, Float prices, String details, Integer orderType) throws Exception {
        Orders orders = new Orders();
        try {
            // 生成订单id
            String ids = "YT" + UUID.randomUUID().toString().replaceAll("\\-", "");
            String orderId = ids.substring(0, ids.length() - 2);
            // 生成订单信息
            orders.setOrderId(orderId);
            orders.setOrderCreateTime(new Date());
            orders.setOrderMoney(prices);
            orders.setOrderSource(sourceId);
            orders.setOrderPayMode(1);
            orders.setOrderGoodsId(commonId);
            orders.setOrderTypeId(orderType);
            orders.setOrderDetials(details);
            orders.setUuid(uuid);

            if (ordersMapper.insert(orders) < 1) {
                throw new Exception("订单创建失败!");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        return orders;
    }

    @Override
    public Map<String, Object> FindOrdersLists(int Pages, int Size) throws Exception {
        Map<String, Object> response = new HashMap<>();
        try {
            // 获取分页的数据
            IPage<OrderLists> page = new Page<>(Pages, Size);
            QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("order_delete", 0);

            ordersMapper.selectOrdersPage(page, queryWrapper);
            response.put("page", Pages);
            response.put("total", page.getTotal());
            response.put("list", page.getRecords());
            response.put("pageSize", Size);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void UpdateOrderCode(String orderId, String codeUrl) throws Exception {
        try {
            QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("order_id", orderId);

            if (ordersMapper.selectCount(queryWrapper) < 1) {
                throw new Exception("订单信息不存在!");
            }
            Orders orders = new Orders();
            orders.setOrderReptyCode(codeUrl);
            if (ordersMapper.update(orders, queryWrapper) < 1) {
                throw new Exception("更新订单二维码失败!");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public void CheckCallBack(Integer orderId) throws Exception {
        try {
            QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id", orderId);
            queryWrapper.eq("order_status", 1);
            if (ordersMapper.selectCount(queryWrapper) < 1) {
                throw new Exception("未支付!");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void UpdateOrderInfo(String orderId, String transactionId) throws Exception {
        try {
            // 查下订单ID是什么订单
            QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("order_id", orderId);
            Orders ordersData = ordersMapper.selectOne(queryWrapper);

            if (ordersData == null){
                throw new Exception("订单信息异常!");
            }

            // 防止重复更新
            if (ordersData.getOrderStatus() != 1) {
                // 更新订单
                Orders orders = new Orders();
                orders.setOrderTransactionId(transactionId);
                orders.setOrderUpdateTime(new Date());
                orders.setOrderPayTime(new Date());
                // 已支付
                orders.setOrderStatus(1);
                QueryWrapper<Orders> queryWrapper2 = new QueryWrapper<>();
                queryWrapper2.eq("order_id", orderId);
                if (ordersMapper.update(orders, queryWrapper2) < 1){
                    throw new Exception("更新订单信息失败!");
                }

                // 判断当前订单是什么订单,如果是集群还要更新集群状态
                if (ordersData.getOrderTypeId() == 1) {
                    // 更新集群状态
                    QueryWrapper<ServiceDeploy> queryWrapper1 = new QueryWrapper<>();
                    queryWrapper1.eq("cluster_id", ordersData.getOrderGoodsId());

                    ServiceDeploy serviceDeploy = new ServiceDeploy();
                    serviceDeploy.setClusterUpdatetime(new Date());
                    // 在前台暴露出来
                    serviceDeploy.setClusterDelete(1);
                    // 让其进入配置状态
                    serviceDeploy.setClusterServiceStatus(2);
                    if (clusterMapper.update(serviceDeploy, queryWrapper1) < 1) {
                        throw new Exception("更新集群状态失败!");
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> FindUserOrdersLists(int Page, int Size, String uuid) throws Exception {
        Map<String, Object> response = new HashMap<>();
        try {
            // 获取分页的数据
            IPage<Orders> page = new Page<>(Page, Size);
            QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uuid", uuid);
            queryWrapper.eq("order_delete", 0);
            queryWrapper.orderByDesc("order_createtime");
            ordersMapper.selectPage(page, queryWrapper);

            ArrayList<Map<String, Object>> records = new ArrayList<>();
            for (Orders orders: page.getRecords()){
                Map<String, Object> items = new HashMap<>();
                items.put("id", orders.getId());
                items.put("orderId", orders.getOrderId());
                switch (orders.getOrderTypeId()) {
                    case 1:
                        items.put("orderType", "集群订单");
                        break;
                    case 2:
                        items.put("orderType", "Dockerfile订单");
                        break;
                    case 3:
                        items.put("orderType", "开放平台服务订单");
                        break;
                    case 4:
                        items.put("orderType", "版本升级订单");
                        break;

                    case 5:
                        items.put("orderType", "套餐组合订单");
                        break;
                }
                items.put("orderTime", orders.getOrderCreateTime());
                items.put("orderPrice", orders.getOrderMoney());
                items.put("orderStatus", orders.getOrderStatus());
                records.add(items);
            }

            response.put("page", Page);
            response.put("total", page.getTotal());
            response.put("list", records);
            response.put("pageSize", Size);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
        return response;
    }

    @Override
    public Map<String, Object> FindUserOrdersInfo(Integer id, String uuid) throws Exception {
        Map<String, Object> response = new HashMap<>();
        try {
            QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id", id);
            queryWrapper.eq("uuid", uuid);
            Orders orders = ordersMapper.selectOne(queryWrapper);
            if (orders == null){
                throw new Exception("订单信息查询异常，请勿越权查询!");
            }
            switch (orders.getOrderTypeId()) {
                case 1:
                    response.put("orderType", "集群订单");
                    break;
                case 2:
                    response.put("orderType", "Dockerfile订单");
                    break;
                case 3:
                    response.put("orderType", "开放平台服务订单");
                    break;
                case 4:
                    response.put("orderType", "版本升级订单");
                    break;

                case 5:
                    response.put("orderType", "套餐组合订单");
                    break;
            }
            response.put("orderId", orders.getOrderId());
            response.put("orderTime", orders.getOrderCreateTime());
            response.put("orderPrice", orders.getOrderMoney());
            response.put("orderStatus", orders.getOrderStatus());
            response.put("orderReptyCode", orders.getOrderReptyCode());
            response.put("orderDetials", orders.getOrderDetials());
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
        return response;
    }
}
