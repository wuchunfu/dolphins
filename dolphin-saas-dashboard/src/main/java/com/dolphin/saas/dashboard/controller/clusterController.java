package com.dolphin.saas.dashboard.controller;

import com.dolphin.saas.commons.clouds.KuberContor;
import com.dolphin.saas.commons.clouds.aliyun.feature.EcsServ;
import com.dolphin.saas.commons.clouds.aliyun.feature.SecurityGroupServ;
import com.dolphin.saas.commons.clouds.comFinal.MasterCommon;
import com.dolphin.saas.commons.clouds.tencent.ClusterServPlugin;
import com.dolphin.saas.commons.clouds.tencent.SecGroupServPlugin;
import com.dolphin.saas.commons.clouds.tencent.entity.KubIntity;
import com.dolphin.saas.commons.clouds.tencent.entity.WxPayKv;
import com.dolphin.saas.commons.clouds.tencent.feature.PayServ;
import com.dolphin.saas.dashboard.common.RedisUtils;
import com.dolphin.saas.entity.Orders;
import com.dolphin.saas.entity.ServiceDeployWorkConfig;
import com.dolphin.saas.searchs.ClusterSearch;
import com.dolphin.saas.service.ClusterService;
import com.dolphin.saas.service.OrderService;
import com.dolphin.saas.service.VendorsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/cluster")
@Api(tags = "集群管理接口", description = "7个")
public class clusterController extends MasterCommon {
    @Resource
    private RedisUtils redisUtils;

    @Resource
    private ClusterService clusterService;

    @Resource
    private OrderService orderService;

    @Resource
    private VendorsService vendorsService;

    /**
     * 获取安全组策略
     *
     * @param cid 集群id
     * @return
     */
    @ApiOperation("获取安全组策略")
    @RequestMapping(value = "/getFirewall", method = RequestMethod.POST)
    public Map<String, Object> GetFirewall(@RequestHeader Map<String, String> headers, Long cid) {
        try {
            ArrayList<Map<String, String>> results = new ArrayList<>();
            if (cid == null) {
                return JsonResponseStr(-1, "failed", "集群id不能为空");
            }
            // 根据集群ID获取AK、安全组ID、归属区域
            Map<String, String> clusterInfo = clusterService.GetClusterMessage(cid, redisUtils.getUUID(headers.get("token")));

            // 获取防火墙策略
            switch (Integer.parseInt(clusterInfo.get("cloudId"))) {
                case 1:
                    Map<String, Object> paramets = new HashMap<>();
                    paramets.put("region", clusterInfo.get("regionId"));
                    SecurityGroupServ securityGroupServ = new SecurityGroupServ(clusterInfo.get("accessKey"),
                            clusterInfo.get("accessSecret"), paramets);
                    results = securityGroupServ.getEgressLists(clusterInfo.get("securityGroupId"));
                    break;

                case 2:
                    SecGroupServPlugin secGroupServPlugin = new SecGroupServPlugin(
                            clusterInfo.get("accessKey"),
                            clusterInfo.get("accessSecret"));
                    results = secGroupServPlugin.getEgressLists(
                            clusterInfo.get("securityGroupId"),
                            clusterInfo.get("regionId"));
                    break;
            }
            return JsonResponse(1, results, "读取成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 获取HIDS的日志
     *
     * @param cid 集群id
     * @return
     */
    @ApiOperation("获取HIDS的日志")
    @RequestMapping(value = "/getHidsLog", method = RequestMethod.POST)
    public Map<String, Object> GetHidsLog(@RequestHeader Map<String, String> headers, Long cid) {
        try {
            ArrayList<Map<String, String>> results = new ArrayList<>();
            if (cid == null) {
                throw new Exception("集群ID不能为空!");
            }
            // 只有在高级版本里才有HIDS
            Map<String, String> clusterInfo = clusterService.GetClusterMessage(cid, redisUtils.getUUID(headers.get("token")));
            if (Integer.parseInt(clusterInfo.get("current")) != 250) {
                throw new Exception("当前不是大型企业，没有主机安全的防护!");
            }
            // 对ES地址进行检索，找到那个索引，把数据查出来
            RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost(clusterInfo.get("esAddr"), 80, "http")));
            // 获取HIDS的索引，判断是否存在
            GetIndexRequest request = new GetIndexRequest("falco-*");
            Boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
            if (!exists) {
                throw new Exception("没有主机安全日志!");
            }
            // 构造HIDS查询的日志
            SearchRequest searchRequest = new SearchRequest("falco-*");
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
//            sourceBuilder.highlighter();
//            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("output", "*");
//            QueryBuilders.matchAllQuery();
//            sourceBuilder.query(termQueryBuilder);
            sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
            searchRequest.source(sourceBuilder);
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

            for (SearchHit documentFields : searchResponse.getHits().getHits()) {
                Map<String, String> items = new HashMap<>();
                System.out.println(documentFields.getSourceAsString());
                items.put("rules", documentFields.getSourceAsMap().get("rule").toString());
                items.put("createTime", documentFields.getSourceAsMap().get("time").toString());
                items.put("level", documentFields.getSourceAsMap().get("priority").toString());
                items.put("alter", documentFields.getSourceAsMap().get("output").toString());
                Map<String, Object> fields = (Map<String, Object>) documentFields.getSourceAsMap().get("output_fields");
                items.put("namespace", fields.get("k8s.ns.name").toString());
                items.put("pod", fields.get("k8s.pod.name").toString());
                items.put("session", fields.get("proc.cmdline").toString());
                results.add(items);
            }
            return JsonResponse(1, results, "查询成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }


//    /**
//     * 获取异常监控的日志
//     *
//     * @param cid 集群id
//     * @return
//     */
//    @ApiOperation("获取异常监控的日志")
//    @RequestMapping(value = "/getSentryLog", method = RequestMethod.POST)
//    public Map<String, Object> GetSentryLog(@RequestHeader Map<String, String> headers, Long cid) {
//        try {
//            if (cid == null) {
//                throw new Exception("集群ID不能为空!");
//            }
//            // 获取集群部署的es的地址
//            // 对ES地址进行检索，找到那个索引，把数据查出来
//            RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("127.0.0.1", 9200, "http")));
//            // 获取HIDS的索引，判断是否存在
//            GetIndexRequest request = new GetIndexRequest("falco");
//            Boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
//            if (exists){
//                // 构造HIDS查询的日志
//                SearchRequest searchRequest = new SearchRequest("falco");
//                SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
//                sourceBuilder.highlighter();
//                TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("cluster.name", "*.*");
//                QueryBuilders.matchAllQuery();
//                sourceBuilder.query(termQueryBuilder);
//                sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
//                searchRequest.source(sourceBuilder);
//                SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
//                System.out.println(JSON.toJSONString(searchResponse.getHits()));
//                for (SearchHit documentFields : searchResponse.getHits().getHits()) {
//                    System.out.println(documentFields.getSourceAsMap());
//                }
//            }
//            return JsonResponseStr(1, "ok", "删除成功!");
//        } catch (Exception e) {
//            return JsonResponseStr(0, "error", e.getMessage());
//        }
//    }


    /**
     * 获取当前存活集群数
     *
     * @return
     */
    @ApiOperation("获取当前存活集群数")
    @RequestMapping(value = "/aliveCluster", method = RequestMethod.POST)
    public Map<String, Object> AliveCluster(@RequestHeader Map<String, String> headers) {
        try {
            return JsonResponseMap(1, clusterService.GetClusterAlive(redisUtils.getUUID(headers.get("token"))), "查询成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 删除集群
     *
     * @param cid 集群id
     * @return
     */
    @ApiOperation("删除集群")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public Map<String, Object> DeleteCluster(@RequestHeader Map<String, String> headers, Long cid) {
        try {
            if (cid == null) {
                return JsonResponseStr(-1, "failed", "集群id不能为空");
            }
            clusterService.DeleteCluster(cid, redisUtils.getUUID(headers.get("token")));
            return JsonResponseStr(1, "ok", "删除成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 判断有没有集群已经部署了
     *
     * @return
     */
    @ApiOperation("判断有没有集群已经部署了")
    @RequestMapping(value = "/judgment", method = RequestMethod.GET)
    public Map<String, Object> Judgment(@RequestHeader Map<String, String> headers) {
        try {
            if (clusterService.CheckClusterStatus(redisUtils.getUUID(headers.get("token")))) {
                return JsonResponseStr(1, "ok", "有集群存在!");
            } else {
                return JsonResponseStr(-1, "failed", "没有集群存在!");
            }
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 获取集群列表
     *
     * @param page 页码
     * @return
     */
    @ApiOperation("获取集群列表")
    @RequestMapping(value = "/lists", method = RequestMethod.POST)
    public Map<String, Object> Lists(@RequestHeader Map<String, String> headers, Integer page, Integer pageSize, ClusterSearch clusterSearch) {
        try {
            if (page == null || page < 1) {
                page = 1;
            }
            if (pageSize == null || pageSize < 10) {
                pageSize = 10;
            }
            return JsonResponseMap(1, clusterService.FindClusterLists(page, pageSize, redisUtils.getUUID(headers.get("token")), clusterSearch), "查询成功!");
        } catch (Exception e) {
            return JsonResponse(0, new ArrayList<>(), e.getMessage());
        }
    }

    /**
     * 获取单个集群信息
     *
     * @param cid 集群id
     * @return
     */
    @ApiOperation("获取单个集群信息")
    @RequestMapping(value = "/read", method = RequestMethod.POST)
    public Map<String, Object> Read(@RequestHeader Map<String, String> headers, Long cid) {
        try {
            Map<String, Object> resultMap = clusterService.ReadCluster(cid, redisUtils.getUUID(headers.get("token")));

            if (resultMap.containsKey("clusterToken")) {
                KubIntity kubIntity = new KubIntity();
                // 加载token和api
                kubIntity.setToken(resultMap.get("clusterToken").toString());
                kubIntity.setApiServer(resultMap.get("clusterApi").toString());
                // 获取所有的服务信息
                KuberContor kuberContor = new KuberContor(kubIntity);
                resultMap.put("servicesLists", kuberContor.NameSpaceServiceLists());
                //删除敏感信息
                resultMap.remove("clusterToken");
                resultMap.remove("clusterApi");
            }
            return JsonResponseMap(1, resultMap, "查询成功!");
        } catch (Exception e) {
            return JsonResponse(0, new ArrayList<>(), e.getMessage());
        }
    }

    /**
     * 获取单个集群部署阶段
     *
     * @param cid 集群id
     * @return
     */
    @ApiOperation("获取单个集群部署阶段")
    @RequestMapping(value = "/readStages", method = RequestMethod.POST)
    public Map<String, Object> ReadStages(@RequestHeader Map<String, String> headers, Long cid) {
        try {
            if (cid == null) {
                return JsonResponseStr(-1, "failed", "集群id不能为空");
            }
            return JsonResponseMap(1, clusterService.ReadClusterStages(cid, redisUtils.getUUID(headers.get("token"))), "查询成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 创建集群询价
     *
     * @param current     并发数
     * @param cloudId     云的Id
     * @param regionId    归属
     * @param zoneId      可用区
     * @param clusterType 集群类型
     * @return
     */
    @ApiOperation("创建集群询价")
    @RequestMapping(value = "/createClusterCalculate", method = RequestMethod.POST)
    public Map<String, Object> CreateClusterCalculate(@RequestHeader Map<String, String> headers, Integer current, Integer cloudId, String regionId, String zoneId, Integer clusterType, Integer payMode) {
        try {
            Map<String, String> ak = vendorsService.FindUUidCloudKey(redisUtils.getUUID(headers.get("token")), cloudId);
            Map<String, Object> results = new HashMap<>();
            switch (cloudId) {
                case 1:
                    Map<String, Object> paramets = new HashMap<>();
                    paramets.put("region", regionId);
                    paramets.put("zone", zoneId);
                    paramets.put("buyType", clusterType);
                    paramets.put("current", current);
                    results = new EcsServ(ak.get("secreTld"), ak.get("secreKey"), paramets).calculateAssets();
                    break;

                case 2:
                    ClusterServPlugin clusterServPlugin = new ClusterServPlugin(ak.get("secreTld"), ak.get("secreKey"));
                    results = clusterServPlugin.calculateAssets(regionId, zoneId, current, clusterType);
                    break;
            }

            ArrayList<Map<String, Object>> serviceLists = new ArrayList<>();

            Map<String, Object> serviceItems;

            switch (payMode) {
                case 1:
                    serviceItems = new HashMap<>();
                    serviceItems.put("typeName", "小型企业（¥ 498 /次）");
                    serviceItems.put("orderPrice", 498.00);
                    serviceItems.put("orderInfo", "部署整体5个服务，具备CI/CD的能力，发布到K8S，送200次免费工程发布。");
                    serviceLists.add(serviceItems);

                    break;

                case 2:
                    serviceItems = new HashMap<>();
                    serviceItems.put("typeName", "中型企业（¥ 1998 /次）");
                    serviceItems.put("orderPrice", 1998.00);
                    serviceItems.put("orderInfo", "部署整体10个服务，具备CI/CD的能力，具备异常监控+链路分析能力，送500次免费发布。");
                    serviceLists.add(serviceItems);

                    break;

                case 3:
                    serviceItems = new HashMap<>();
                    serviceItems.put("typeName", "中型企业（¥ 3998 /次）");
                    serviceItems.put("orderPrice", 3998.00);
                    serviceItems.put("orderInfo", "部署整体15个服务，具备CI/CD的能力，具备监控体系化+链路分析+天然过等级保护二、三级能力，送500次免费发布。");
                    serviceLists.add(serviceItems);
                    break;
            }
            results.put("services", serviceLists);

            return JsonResponseMap(1, results, "查询成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 读取集群的创建信息
     *
     * @param headers   获取token
     * @param clusterId 集群id
     * @param types     0:专家模式,1:普通人
     * @return
     */
    @ApiOperation("读取集群的创建信息")
    @RequestMapping(value = "/readClusterMessage", method = RequestMethod.POST)
    public Map<String, Object> ReadClusterMessage(@RequestHeader Map<String, String> headers, Long clusterId, Integer types) {
        try {
            ArrayList<ServiceDeployWorkConfig> results = clusterService.ReadClusterMessage(clusterId, types, redisUtils.getUUID(headers.get("token")));
            return JsonResponse(1, results, "查询成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }

    /**
     * 创建集群订单
     * 并且生成二维码待支付
     *
     * @param headers     header请求
     * @param current     并发数
     * @param cloudId     云的Id
     * @param regionId    归属
     * @param zoneId      可用区
     * @param clusterType 集群类型
     * @param payMode     支付模式
     * @param sourceId    支付渠道
     * @return
     */
    @ApiOperation("创建集群订单")
    @RequestMapping(value = "/createClusterOrders", method = RequestMethod.POST)
    public Map<String, Object> CreateClusterOrders(@RequestHeader Map<String, String> headers, Integer current, Integer cloudId, String regionId, String zoneId, Integer clusterType, Integer payMode, Integer sourceId) {
        Map<String, Object> results = new HashMap<>();
        try {
            // 创建禁用的集群，支付之后在再起来。
            Long clusterId = clusterService.ClusterCreate(current, cloudId, regionId, zoneId, clusterType, redisUtils.getUUID(headers.get("token")), payMode);

            float prices = 0f;
            String codeUrl = "", details = "", desc = "";
            // 根据模式生成金额
            switch (payMode) {
                case 1:
                    prices = Float.parseFloat("498.00");
                    desc = "元豚科技-小型企业版-" + (int) prices + "元";
                    details = "小型企业集群+200次免费发布";
                    break;

                case 2:
                    prices = Float.parseFloat("1998.00");
                    desc = "元豚科技-中型企业版-" + (int) prices + "元";
                    details = "中型企业集群+500次免费发布";
                    break;

                case 3:
                    prices = Float.parseFloat("3998.00");
                    desc = "元豚科技-大型企业版-" + (int) prices + "元";
                    details = "大型企业集群+1000次免费发布";
                    break;
            }

            // 生成订单
            Orders order = orderService.CreateOrder(clusterId.toString(), sourceId, redisUtils.getUUID(headers.get("token")), prices, details, 1);

            // 生成二维码返回
            if (sourceId == 0) {
                PayServ payServ = new PayServ();
                WxPayKv wxPayKv = new WxPayKv();
                wxPayKv.setDescription(desc);
                wxPayKv.setMoney(prices);
                wxPayKv.setOutTradeNo(order.getOrderId());
                // 回调地址
                wxPayKv.setCallBackUrl("http://backend.api.aidolphins.com/callback/index");
                codeUrl = payServ.wxPay(wxPayKv);
                // 更新回调的二维码地址
                orderService.UpdateOrderCode(order.getOrderId(), codeUrl);
            }

            // 返回结果
            results.put("orderId", order.getId());
            results.put("codeUrl", codeUrl);

            return JsonResponseMap(1, results, "创建成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", "集群创建异常:" + e.getMessage());
        }
    }

    /**
     * 判断是否已经支付
     *
     * @param headers
     * @param orderId 订单ID
     * @return
     */
    @ApiOperation("判断是否已经支付")
    @RequestMapping(value = "/checkOrders", method = RequestMethod.POST)
    public Map<String, Object> checkOrders(@RequestHeader Map<String, String> headers, Integer orderId) {
        try {
            orderService.CheckCallBack(orderId);
            return JsonResponseStr(1, "ok", "已支付");
        } catch (Exception e) {
            return JsonResponseStr(0, "ok", "未支付");
        }
    }


    /**
     * 创建集群执行
     *
     * @param headers     header请求
     * @param current     并发数
     * @param cloudId     云的Id
     * @param regionId    归属
     * @param zoneId      可用区
     * @param clusterType 集群类型
     * @return
     */
    @ApiOperation("创建集群执行")
    @RequestMapping(value = "/createCluster", method = RequestMethod.POST)
    public Map<String, Object> CreateCluster(@RequestHeader Map<String, String> headers, Integer current, Integer cloudId, String regionId, String zoneId, Integer clusterType) {
        try {
            clusterService.ClusterCreate(current, cloudId, regionId, zoneId, clusterType, redisUtils.getUUID(headers.get("token")), 1);
            return JsonResponseStr(1, "ok", "集群创建成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", "集群创建异常:" + e.getMessage());
        }
    }

    @ApiOperation("执行集群资产部署")
    @RequestMapping(value = "/execClusterDeploy", method = RequestMethod.POST)
    public Map<String, Object> ExecClusterDeploy(@RequestHeader Map<String, String> headers, Long cid) {
        try {
            if (cid == null) {
                throw new Exception("CID不能为空!");
            }
            if (!clusterService.ExecClusterDeploy(cid, redisUtils.getUUID(headers.get("token")))) {
                throw new Exception("执行集群资产部署失败!");
            }
            return JsonResponseStr(1, "ok", "执行集群资产部署成功!");
        } catch (Exception e) {
            return JsonResponseStr(0, "error", e.getMessage());
        }
    }
}
