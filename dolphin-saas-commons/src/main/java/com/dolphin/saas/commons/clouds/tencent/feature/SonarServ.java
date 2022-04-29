package com.dolphin.saas.commons.clouds.tencent.feature;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class SonarServ {

    private final String hosts;
    private final String userName;
    private final String passWord;

    public SonarServ(String hosts, String userName, String passWord) {
        this.hosts = hosts;
        this.userName = userName;
        this.passWord = passWord;
    }

    /**
     * 创建TOKEN(30分钟不重复申请)
     *
     * @return
     * @throws Exception
     */
    public String createToken() throws Exception {
        try {
            RestTemplate restTemplate = new RestTemplate();
            //跨域访问表头
            HttpHeaders headers = new HttpHeaders();

            String authString = this.userName + ":" + this.passWord;
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes(StandardCharsets.UTF_8));
            String authStringEnc = new String(authEncBytes);
            headers.add("Authorization", "Basic " + authStringEnc);

            //接口地址
            String url = "http://%s/api/user_tokens/generate";
            url = String.format(url, this.hosts);

            MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
            String tokenName = String.valueOf(new Random().nextInt(1000000));
            multiValueMap.add("name", tokenName);

            //放入Http传输的数据
            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(multiValueMap, headers);
            //访问接口并获取返回值
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
            //输出接口所返回过来的值
            Map<String, Object> map = (Map) JSON.parse(responseEntity.getBody());
            String token = map.get("token").toString();
            return token;
        } catch (Exception e) {
            log.error("[服务][Sonar]获取Token异常,信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 获取整体的扫描结果
     *
     * @param ProjectKey 发布的版本信息, 比如：dolphin-saas-web:2022_01_27_114559
     * @return
     * @throws Exception
     */
    @Retryable(value = Exception.class, maxAttempts = 5)
    public Map<String, Object> getComponent(String ProjectKey) throws Exception {
        Map<String, Object> results = new HashMap<>();
        try {
            RestTemplate restTemplate = new RestTemplate();
            //跨域访问表头
            HttpHeaders headers = new HttpHeaders();

            // 认证部分
            String authString = this.userName + ":" + this.passWord;
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes(StandardCharsets.UTF_8));
            String authStringEnc = new String(authEncBytes);
            headers.add("Authorization", "Basic " + authStringEnc);

            // 获取项目的扫描状态
            String statusUrl = "http://%s/api/qualitygates/project_status?projectKey=%s";
            statusUrl = String.format(statusUrl, this.hosts, ProjectKey);

            ResponseEntity<String> responseStatusEntity = restTemplate.exchange(
                    statusUrl,
                    HttpMethod.GET,
                    new HttpEntity<String>(headers),
                    String.class
            );

            JSONObject projectScanStatusResponse = JSON.parseObject(responseStatusEntity.getBody());
            Map<String, String> ScanTaskStatusInfo = (Map<String, String>) projectScanStatusResponse.get("projectStatus");

            // 判断是否已经扫描结束
            if (ScanTaskStatusInfo.get("status").equals("OK")) {
                // 扫描结束，查扫描结果
                String reportUrl = "http://%s/api/measures/component?component=%s&additionalFields=periods,metrics&metricKeys=code_smells,bugs,vulnerabilities,ncloc,complexity,violations";
                reportUrl = String.format(reportUrl, hosts, ProjectKey);

                ResponseEntity<String> responseReportEntity = restTemplate.exchange(
                        reportUrl,
                        HttpMethod.GET,
                        new HttpEntity<String>(headers),
                        String.class
                );

                // 获取扫描结果统计信息
                Map<String, Object> ReportsMeasuresInfo = JSON.parseObject(responseReportEntity.getBody(), Map.class);
                Map<String, Object> ReportsTotalsObject = (Map<String, Object>) ReportsMeasuresInfo.get("component");

                JSONArray ReportsTotalsInfo = JSON.parseArray(JSON.toJSONString(ReportsTotalsObject.get("measures")));

                for (Object TotalItems : ReportsTotalsInfo) {
                    Map<String, String> TotalMessage = (Map<String, String>) TotalItems;
                    switch (TotalMessage.get("metric")) {
                        case "ncloc":
                            // 检查行数
                            results.put("codeline", Long.parseLong(TotalMessage.get("value")));
                            break;

                        case "violations":
                            // 不规范
                            results.put("violations", Long.parseLong(TotalMessage.get("value")));
                            break;
                    }
                }

                // 获取报告详细结果
                String detialUrl = "http://%s/api/issues/search?additionalFields=comments,rules&componentKeys=%s&types=CODE_SMELL,BUG,VULNERABILITY&ps=50&s=CREATION_DATE";
                detialUrl = String.format(detialUrl, hosts, ProjectKey);

                ResponseEntity<String> responseDetialEntity = restTemplate.exchange(
                        detialUrl,
                        HttpMethod.GET,
                        new HttpEntity<String>(headers),
                        String.class
                );

                Map<String, Object> responseDetialMapObject = (Map) JSON.parse(responseDetialEntity.getBody());

                // 最终ISSUE结果汇聚
                ArrayList<Map<String, Object>> issuesAllDetials = new ArrayList<>();

                // 统计数据
                Integer high = 0;
                Integer fatal = 0;
                Integer code_smell = 0;
                Integer bugs = 0;
                Integer vulnerabilities = 0;

                // 获取总条数，生成页码数
                Integer total = Math.round(Float.valueOf(Integer.valueOf(responseDetialMapObject.get("total").toString())) / 500);
                for (int i = 1; i < total; i++) {
                    detialUrl = "http://%s/api/issues/search?additionalFields=comments,rules&componentKeys=%s&types=CODE_SMELL,BUG,VULNERABILITY&ps=500&p=%s&s=CREATION_DATE";
                    detialUrl = String.format(detialUrl, hosts, ProjectKey, i);

                    ResponseEntity<String> responseDetialEntity1 = restTemplate.exchange(
                            detialUrl,
                            HttpMethod.GET,
                            new HttpEntity<String>(headers),
                            String.class
                    );

                    Map<String, Object> responseDetialMapObject1 = (Map) JSON.parse(responseDetialEntity1.getBody());
                    JSONArray issuesGroup = JSON.parseArray(JSON.toJSONString(responseDetialMapObject1.get("issues")));

                    for (Object issuesItems : issuesGroup) {
                        Map<String, Object> issuesDetials = (Map<String, Object>) issuesItems;
                        Map<String, Object> infoItems = new HashMap<>();
                        // 只采集开放的问题
                        if (issuesDetials.get("status").toString().equals("OPEN")) {
                            // 把key放到查询问题代码行数去
                            String codeDetials = "http://%s/api/sources/issue_snippets?issueKey=%s";
                            codeDetials = String.format(codeDetials, hosts, issuesDetials.get("key"));

                            ResponseEntity<String> responseCodeDetialsEntity = restTemplate.exchange(
                                    codeDetials,
                                    HttpMethod.GET,
                                    new HttpEntity<String>(headers),
                                    String.class
                            );

                            Map<String, Object> responseCodeDetialMapObject1 = (Map) JSON.parse(responseCodeDetialsEntity.getBody());
                            // 获取不到就放弃
                            if (responseCodeDetialMapObject1 == null) {
                                continue;
                            }
                            // 确保不会出异常
                            if (responseCodeDetialMapObject1.keySet().size() > 0) {
                                Map<String, Object> infoDetails = (Map<String, Object>) responseCodeDetialMapObject1.get(responseCodeDetialMapObject1.keySet().toArray()[0]);

                                List<String> code = new ArrayList<>();
                                for (Map<String, Object> sourceItems : (List<Map<String, Object>>) infoDetails.get("sources")) {
                                    code.add(sourceItems.get("line").toString() + ":" + sourceItems.get("code"));
                                }
                                // 写入具体的代码
                                infoItems.put("code", String.join("<br />", code));
                            } else {
                                infoItems.put("code", "");
                            }

                            // 触发的规则
                            infoItems.put("ruleName", issuesDetials.get("rule").toString());
                            // 风险级别
                            infoItems.put("severity", issuesDetials.get("severity").toString());
                            if (issuesDetials.get("severity").toString().equals("INFO") || issuesDetials.get("severity").toString().equals("MINOR")) {
                                // 不重要的问题不采集，没有意义
                                continue;
                            } else {
                                // 做个数据统计
                                switch (issuesDetials.get("severity").toString()) {
                                    case "MAJOR":
                                        high++;
                                        break;

                                    case "CRITICAL":
                                    case "BLOCKER":
                                        fatal++;
                                        break;
                                }
                            }
                            // 文件地址
                            infoItems.put("fileName", issuesDetials.get("component").toString().split(":")[2]);
                            // 问题行数
                            infoItems.put("lineNumber", issuesDetials.get("line"));
                            // 问题阐述
                            infoItems.put("issuesMessage", issuesDetials.get("message").toString());
                            // 问题类型
                            infoItems.put("issuesType", issuesDetials.get("type").toString());
                            switch (issuesDetials.get("type").toString()) {
                                case "CODE_SMELL":
                                    code_smell++;
                                    break;

                                case "BUG":
                                    bugs++;
                                    break;

                                case "VULNERABILITY":
                                    vulnerabilities++;
                                    break;
                            }

                            issuesAllDetials.add(infoItems);
                        }
                    }
                }
                results.put("issuesAllDetials", issuesAllDetials);
                // 风险统计
                results.put("high", high);
                results.put("fatal", fatal);
                results.put("code_smell", code_smell);
                results.put("bugs", bugs);
                results.put("vulnerabilities", vulnerabilities);
            }
        } catch (Exception e) {
            log.error("[基础服务][Sonar]获取数据失败:{}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return results;
    }
}
