package com.dolphin.saas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dolphin.saas.common.BaseTools;
import com.dolphin.saas.entity.*;
import com.dolphin.saas.entity.vo.DashboardEcharts;
import com.dolphin.saas.entity.vo.DashboardLists;
import com.dolphin.saas.mapper.*;
import com.dolphin.saas.service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service("dashboardService")
public class DashboardServiceimpl extends BaseTools implements DashboardService {
    @Resource
    private DeployMapper deployMapper;

    @Resource
    private EngineeringMapper engineeringMapper;

    @Resource
    private EngineerAnalyzeMapper engineerAnalyzeMapper;

    @Resource
    private MerchantMapper merchantMapper;

    @Resource
    private OrdersMapper ordersMapper;

    @Resource
    private ClusterMapper clusterMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public Map<String, Object> ProjectJobs(String uuid) {
        Map<String, Object> results = new HashMap<>();

        ArrayList<String> uUidList = this.orgUUidList(uuid);
        QueryWrapper<Engineer> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("ht_engineer.uuid", uUidList);
        queryWrapper.eq("engineer_status", 2);
        queryWrapper.orderByDesc("engineer_createtime");

        List<DashboardLists> engineerList = engineeringMapper.engineerCountArr(queryWrapper);

        results.put("total", engineerList.size());
        results.put("list", engineerList);

        return results;
    }

    @Override
    public Map<String, Object> ReleaseJobs(String uuid) {
        Map<String, Object> results = new HashMap<>();

        QueryWrapper<Release> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ht_release_jobs.uuid", uuid);
        queryWrapper.orderByDesc("release_job_createtime");

        List<DashboardLists> engineerList = deployMapper.releaseCountArr(queryWrapper);

        results.put("total", engineerList.size());
        results.put("list", engineerList);

        return results;
    }

    @Override
    public Map<String, Object> ReleaseDayJobs(String uuid) throws Exception {
        Map<String, Object> results = new HashMap<>();
        // 获取今天的数据
        try {
            QueryWrapper<Release> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("ht_release_jobs.uuid", uuid);
            queryWrapper.last("AND TO_DAYS(release_job_createtime) = TO_DAYS(NOW())");
            queryWrapper.select("release_id");
            int todayCount = deployMapper.selectCount(queryWrapper);

            // 获取昨天的数据
            QueryWrapper<Release> queryWrapper2 = new QueryWrapper<>();
            queryWrapper2.eq("ht_release_jobs.uuid", uuid);
            queryWrapper2.last("AND TO_DAYS(release_job_createtime) = TO_DAYS(NOW()) - TO_DAYS(release_job_createtime) <= 1");
            queryWrapper2.select("release_id");
            int lastdayCount = deployMapper.selectCount(queryWrapper2);

            // 计算
            float counts;

            // 如果上周的内容等于0，则默认100%增长
            // 如果本周的内容等于0，则默认-100%的增长
            if (todayCount == 0 && lastdayCount > 0) {
                counts = -100;
            } else if (lastdayCount == 0 && todayCount > 0) {
                counts = 100;
            } else if (lastdayCount == 0 && todayCount == 0) {
                counts = 0;
            } else {
                // 如果都不是，就按照公式来算
                counts = new BigDecimal(Float.valueOf((todayCount - lastdayCount)) / Float.valueOf(lastdayCount) * 100)
                        .setScale(2, RoundingMode.HALF_UP).floatValue();
            }

            results.put("total", todayCount);
            results.put("compare", counts);
        } catch (Exception e) {
            throw new Exception("ReleaseDayJobs:" + e);
        }

        return results;
    }

    @Override
    public Map<String, Object> ReleaseWeekJobs(String uuid) throws Exception {
        Map<String, Object> results = new HashMap<>();

        try {
            // 获取本周
            QueryWrapper<Release> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uuid", uuid);
            queryWrapper.last("AND YEARWEEK(date_format(release_job_createtime,'%Y-%m-%d'), 1) = YEARWEEK(now(), 1)");
            int thisWeekCount = deployMapper.selectCount(queryWrapper);

            // 获取上周
            QueryWrapper<Release> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("uuid", uuid);
            queryWrapper1.last("AND YEARWEEK(date_format(release_job_createtime,'%Y-%m-%d'), 1) = YEARWEEK(now(), 1)-1");
            int lastWeekCount = deployMapper.selectCount(queryWrapper1);

            // 计算
            float counts;

            // 如果上周的内容等于0，则默认100%增长
            // 如果本周的内容等于0，则默认-100%的增长
            if (thisWeekCount == 0 && lastWeekCount > 0) {
                counts = -100;
            } else if (lastWeekCount == 0 && thisWeekCount > 0) {
                counts = 100;
            } else if (lastWeekCount == 0 && thisWeekCount == 0) {
                counts = 0;
            } else {
                // 如果都不是，就按照公式来算
                counts = new BigDecimal(
                        Float.valueOf((thisWeekCount - lastWeekCount)) / Float.valueOf(lastWeekCount) * 100)
                        .setScale(2, RoundingMode.HALF_UP).floatValue();
            }

            results.put("total", thisWeekCount);
            results.put("compare", counts);
        } catch (Exception e) {
            throw new Exception("ReleaseWeekJobs:" + e);
        }

        return results;
    }

    @Override
    public Map<String, Object> ReleaseRiskJobs(String uuid) throws Exception {
        Map<String, Object> results = new HashMap<>();
        try {
            // 查当前的组织数据
            ArrayList<String> UUidList = this.orgUUidList(uuid);

            // 查uuid所有关联的项目,对应的engineerId
            QueryWrapper<Engineer> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("uuid", UUidList);
            queryWrapper.select("engineer_id");
            List<Engineer> engineerList = engineeringMapper.selectList(queryWrapper);

            if (engineerList.size() > 0) {
                ArrayList<Long> engineerIds = new ArrayList<>();
                for (Engineer engineer : engineerList) {
                    engineerIds.add(engineer.getEngineerId());
                }

                // 获取本周
                QueryWrapper<EngineerAnalyze> queryWrapper2 = new QueryWrapper<>();
                queryWrapper2.in("engineer_id", engineerIds);
                queryWrapper2.last("AND yearweek(date_format(engineer_createtime,'%Y-%m-%d'), 1) = yearweek(now(), 1)");
                queryWrapper2.select("engineer_id");
                int thisWeekCount = engineerAnalyzeMapper.selectCount(queryWrapper2);

                // 获取上周
                QueryWrapper<EngineerAnalyze> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.in("engineer_id", engineerIds);
                queryWrapper1.last("AND yearweek(date_format(engineer_createtime,'%Y-%m-%d'), 1) = yearweek(now(), 1)-1");
                queryWrapper1.select("engineer_id");
                int lastWeekCount = engineerAnalyzeMapper.selectCount(queryWrapper1);

                float counts;

                // 如果上周的内容等于0，则默认100%增长
                // 如果本周的内容等于0，则默认-100%的增长
                if (thisWeekCount == 0 && lastWeekCount > 0) {
                    counts = -100;
                } else if (lastWeekCount == 0 && thisWeekCount > 0) {
                    counts = 100;
                } else if (lastWeekCount == 0 && thisWeekCount == 0) {
                    counts = 0;
                } else {
                    // 如果都不是，就按照公式来算
                    counts = new BigDecimal(
                            Float.valueOf((thisWeekCount - lastWeekCount)) / Float.valueOf(lastWeekCount) * 100)
                            .setScale(2, RoundingMode.HALF_UP).floatValue();
                }

                results.put("total", thisWeekCount);
                results.put("compare", counts);
            } else {
                results.put("total", 0);
                results.put("compare", 0);
            }
        } catch (Exception e) {
            throw new Exception("ReleaseRiskJobs:" + e);
        }

        return results;
    }

    @Override
    public Map<String, Object> ReleaseAllJobs(String StartTime, String EndTime, String uuid) throws Exception {
        Map<String, Object> results = new HashMap<>();

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date endtime = formatter.parse(EndTime);

            Calendar calendar = new GregorianCalendar();
            calendar.setTime(endtime);
            calendar.add(Calendar.DATE,+1);
            String EndTimeParam = formatter.format(calendar.getTime());

            // 查出对应的日期段统计数据(发布数据)
            QueryWrapper<Release> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("ht_release_jobs.uuid", uuid);
            queryWrapper.eq("release_job_status", 4);
            queryWrapper.between("release_job_createtime", StartTime, EndTimeParam);
            queryWrapper.groupBy("c_days");
            List<DashboardEcharts> releaseList = deployMapper.releaseCharts(queryWrapper);

            // 查出对应的日期段统计数据(回滚数据)
            QueryWrapper<Release> queryWrapper2 = new QueryWrapper<>();
            queryWrapper2.eq("ht_release_jobs.uuid", uuid);
            queryWrapper2.eq("release_job_status", 6);
            queryWrapper2.between("release_job_createtime", StartTime, EndTimeParam);
            queryWrapper2.groupBy("c_days");
            List<DashboardEcharts> rollbackList = deployMapper.releaseCharts(queryWrapper2);

            // 生成对应时间段的日期数据
            ArrayList<String> dateList;
            try {
                dateList = this.getData(StartTime, EndTime);
            } catch (Exception e) {
                throw new Exception("获取时间异常!");
            }

            // 生成日期对应的发布数据
            Map<String, Long> dateMap = new HashMap<>();
            for (DashboardEcharts dashboardEcharts : releaseList) {
                dateMap.put(dashboardEcharts.getCdays(), dashboardEcharts.getCounts());
            }

            // 生成日期对应的回滚数据
            Map<String, Long> dateMap2 = new HashMap<>();
            for (DashboardEcharts dashboardEcharts : rollbackList) {
                dateMap2.put(dashboardEcharts.getCdays(), dashboardEcharts.getCounts());
            }

            // 循环组成发布数据 * 循环组成回滚数据
            ArrayList<Long> releaseCount = new ArrayList<>();
            ArrayList<Long> rollbackCount = new ArrayList<>();

            for (String dateItems : dateList) {
                if (dateMap.containsKey(dateItems)) {
                    releaseCount.add(dateMap.get(dateItems));
                } else {
                    releaseCount.add(Long.parseLong("0"));
                }
                if (dateMap2.containsKey(dateItems)) {
                    rollbackCount.add(dateMap2.get(dateItems));
                } else {
                    rollbackCount.add(Long.parseLong("0"));
                }
            }

            // 时间数据 & 发布数据 & 回滚数据
            results.put("dateTime", dateList);
            results.put("release", releaseCount);
            results.put("rollback", rollbackCount);
        } catch (Exception e) {
            throw new Exception("获取发布统计列表异常!");
        }

        return results;
    }

    @Override
    public Map<String, Integer> merchantCount() throws Exception {
        Map<String, Integer> results = new HashMap<>();

        try {
            QueryWrapper<Merchant> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.in("merchant_status", 0, 1, 3);
            results.put("merchantCount", merchantMapper.selectCount(queryWrapper1));

            QueryWrapper<Merchant> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("merchant_status", 3);
            results.put("certifiedCompanyCount", merchantMapper.selectCount(queryWrapper));
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return results;
    }

    @Override
    public Map<String, Integer> orderCount() throws Exception {
        Map<String, Integer> results = new HashMap<>();
        try {
            QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("order_delete", 0);
            queryWrapper.select("id", "order_status");
            List<Orders> ordersList = ordersMapper.selectList(queryWrapper);

            // 待支付
            int paidOrderCount = 0;
            int paidOrderAmountCount = 0;

            if (ordersList.size() > 0) {
                for (Orders orders : ordersList) {
                    switch (orders.getOrderStatus()) {
                        case 0:
                            paidOrderCount = paidOrderCount + 1;
                            break;
                        case 1:
                            paidOrderAmountCount = paidOrderAmountCount + 1;
                            break;
                    }
                }
            }
            results.put("paidOrderCount", paidOrderCount);
            results.put("paidOrderAmountCount", paidOrderAmountCount);
            results.put("accumulatedProfitCount", 0);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return results;
    }

    @Override
    public Integer clusterCount() throws Exception {
        try {
            QueryWrapper<ServiceDeploy> queryWrapper = new QueryWrapper<>();
            return clusterMapper.selectCount(queryWrapper);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Integer releasesCount() throws Exception {
        try {
            QueryWrapper<Release> queryWrapper = new QueryWrapper<>();
            return deployMapper.selectCount(queryWrapper);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Integer memberCount() throws Exception {
        try {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            return userMapper.selectCount(queryWrapper);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 根据字符串时间，获取开始时间和结束时间的日期列表
     *
     * @param StartTime 开始时间
     * @param EndTime   结束时间
     * @return 时间数组
     * @throws Exception 标准异常
     */
    protected ArrayList<String> getData(String StartTime, String EndTime) throws Exception {
        ArrayList<String> dateList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date starttime = sdf.parse(StartTime);
            Date endtime = sdf.parse(EndTime);
            long diff = endtime.getTime() - starttime.getTime();
            long nd = 1000 * 24 * 60 * 60;

            for (int i = 0; i <= diff / nd; i++) {
                String day = sdf.format(DateUtils.addDays(starttime, i));
                dateList.add(day);
            }

        } catch (ParseException e) {
            throw new Exception(e.getMessage());
        }
        return dateList;
    }
}
