package com.dolphin.saas.commons.clouds;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
public class NexusContor {
    private final String baseUrl;
    private final HttpHeaders headers;
    private final Boolean https;

    private ArrayList<String> repositoriesLists;

    public NexusContor(String baseUrl, String userName, String passWord, Boolean https) throws Exception {
        this.baseUrl = baseUrl;
        this.https = https;
        try {
            this.headers = new HttpHeaders();
            String authString = userName + ":" + passWord;
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes(StandardCharsets.UTF_8));
            String authStringEnc = new String(authEncBytes);
            this.headers.add("Authorization", "Basic " + authStringEnc);
        } catch (Exception e) {
            log.error("[服务][Nexus3]获取Token异常,信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 获取所有的仓库
     *
     * @return
     * @throws Exception
     */
    public ArrayList<String> getbobStore() throws Exception {
        ArrayList<String> results = new ArrayList<>();
        try {
            RestTemplate restTemplate = new RestTemplate();
            //接口地址
            String url;
            if (this.https){
                url = "https://%s/service/rest/v1/blobstores";
            }else{
                url = "http://%s/service/rest/v1/blobstores";
            }
            url = String.format(url, this.baseUrl);
            //接口地址
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<String>(this.headers),
                    String.class
            );

            //输出接口所返回过来的值
            JSONArray jarray = JSON.parseArray(responseEntity.getBody());

            for (Object o : jarray) {
                Map<String, Object> obj = (Map<String, Object>) o;
                results.add(obj.get("name").toString());
            }

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return results;
    }

    /**
     * 初始化仓库
     * @return
     * @throws Exception
     */
    public NexusContor addStore() throws Exception {
        try {
            ArrayList<String> storeLists = this.getbobStore();

            if (!storeLists.contains("maven-use")){
                RestTemplate restTemplate = new RestTemplate();
                this.headers.setContentType(MediaType.APPLICATION_JSON);
                //接口地址
                String url;
                if (this.https){
                    url = "https://%s/service/rest/v1/blobstores/file";
                }else{
                    url = "http://%s/service/rest/v1/blobstores/file";
                }
                //接口地址
                url = String.format(url, this.baseUrl);

                JSONObject paramets = new JSONObject();
                paramets.put("softQuota", "null");
                paramets.put("name", "maven-use");
                paramets.put("path", "maven-use");

                HttpEntity<String> request =
                        new HttpEntity<>(paramets.toString(), this.headers);

                ResponseEntity<String> responseEntityStr = restTemplate.
                        postForEntity(url, request, String.class);

                if (responseEntityStr.getStatusCodeValue() != 204) {
                    throw new Exception("创建Store失败!");
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return this;
    }

    /**
     * 获取所有的Repositories
     *
     * @return
     * @throws Exception
     */
    public ArrayList<String> getAllRepositories() throws Exception {
        ArrayList<String> results = new ArrayList<>();
        try {
            RestTemplate restTemplate = new RestTemplate();
            //接口地址
            String url;
            if (this.https){
                url = "https://%s/service/rest/v1/repositories";
            }else{
                url = "http://%s/service/rest/v1/repositories";
            }
            url = String.format(url, this.baseUrl);

            //接口地址
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<String>(this.headers),
                    String.class
            );

            //输出接口所返回过来的值
            JSONArray jarray = JSON.parseArray(responseEntity.getBody());

            for (Object o : jarray) {
                Map<String, Object> obj = (Map<String, Object>) o;
                results.add(obj.get("name").toString());
            }
        } catch (Exception e) {
            log.error("getAllRepositories, error: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return results;
    }

    /**
     * 清理所有不相关的
     *
     * @throws Exception
     */
    public NexusContor cleanRepositories() throws Exception {
        List<String> deleteLists = Arrays.asList(
                "maven-snapshots", "maven-central", "nuget-group",
                "nuget.org-proxy", "maven-releases", "nuget-hosted", "maven-public");
        try {
            RestTemplate restTemplate = new RestTemplate();

            this.repositoriesLists = this.getAllRepositories();
            for (String respositoriesItems : this.repositoriesLists) {
                if (deleteLists.contains(respositoriesItems)) {
                    //接口地址
                    String url;
                    if (this.https){
                        url = "https://%s/service/rest/v1/repositories/%s";
                    }else{
                        url = "http://%s/service/rest/v1/repositories/%s";
                    }
                    url = String.format(url, this.baseUrl, respositoriesItems);
                    //接口地址
                    ResponseEntity<String> responseEntity = restTemplate.exchange(
                            url,
                            HttpMethod.DELETE,
                            new HttpEntity<String>(this.headers),
                            String.class
                    );
                    if (responseEntity.getStatusCodeValue() != 204) {
                        throw new Exception(String.format("删除仓库失败: %s", respositoriesItems));
                    }
                }
            }
        } catch (Exception e) {
            log.error("cleanRepositories, error: {}" , e.getMessage());
            throw new Exception(e.getMessage());
        }
        return this;
    }

    /**
     * 初始化HOSTS
     * @return
     * @throws Exception
     */
    public NexusContor addHosts() throws Exception {
        try {
            if (!this.repositoriesLists.contains("maven-local")){
                RestTemplate restTemplate = new RestTemplate();
                this.headers.setContentType(MediaType.APPLICATION_JSON);
                //接口地址
                String url;
                if (this.https){
                    url = "https://%s/service/rest/v1/repositories/maven/hosted";
                }else{
                    url = "http://%s/service/rest/v1/repositories/maven/hosted";
                }
                url = String.format(url, this.baseUrl);

                JSONObject storageJSon = new JSONObject();
                storageJSon.put("blobStoreName", "maven-use");
                storageJSon.put("strictContentTypeValidation", true);
                storageJSon.put("writePolicy", "ALLOW");

                JSONObject mavenJSon = new JSONObject();
                mavenJSon.put("versionPolicy", "RELEASE");
                mavenJSon.put("layoutPolicy", "STRICT");
                mavenJSon.put("contentDisposition", "INLINE");

                JSONObject componentJSon = new JSONObject();
                componentJSon.put("proprietaryComponents", false);

                JSONObject paramets = new JSONObject();
                paramets.put("name", "maven-local");
                paramets.put("online", true);
                paramets.put("storage", storageJSon);
                paramets.put("cleanup", null);
                paramets.put("maven", mavenJSon);
                paramets.put("component", componentJSon);


                HttpEntity<String> request =
                        new HttpEntity<>(paramets.toString(), this.headers);

                ResponseEntity<String> responseEntityStr = restTemplate.
                        postForEntity(url, request, String.class);

                if (responseEntityStr.getStatusCodeValue() != 201) {
                    throw new Exception("创建hosts失败!");
                }
            }
        } catch (Exception e) {
            log.error("addHosts, error: {}" , e.getMessage());
            throw new Exception(e.getMessage());
        }
        return this;
    }

    /**
     * 初始化阿里云proxy
     * @return
     * @throws Exception
     */
    public NexusContor addAliyunProxy() throws Exception {
        try {
            if (!this.repositoriesLists.contains("proxy-maven")) {
                RestTemplate restTemplate = new RestTemplate();
                this.headers.setContentType(MediaType.APPLICATION_JSON);
                //接口地址
                String url;
                if (this.https){
                    url = "https://%s/service/rest/v1/repositories/maven/proxy";
                }else{
                    url = "http://%s/service/rest/v1/repositories/maven/proxy";
                }

                url = String.format(url, this.baseUrl);

                JSONObject storageJSon = new JSONObject();
                storageJSon.put("blobStoreName", "maven-use");
                storageJSon.put("strictContentTypeValidation", true);
                storageJSon.put("writePolicy", "ALLOW");

                JSONObject mavenJSon = new JSONObject();
                mavenJSon.put("versionPolicy", "RELEASE");
                mavenJSon.put("layoutPolicy", "STRICT");
                mavenJSon.put("contentDisposition", "INLINE");

                JSONObject negativeCacheJSon = new JSONObject();
                negativeCacheJSon.put("enabled", true);
                negativeCacheJSon.put("timeToLive", 1440);

                JSONObject httpClientJSon = new JSONObject();
                httpClientJSon.put("blocked", false);
                httpClientJSon.put("autoBlock", true);
                httpClientJSon.put("connection", null);
                httpClientJSon.put("authentication", null);

                JSONObject proxyJSon = new JSONObject();
                proxyJSon.put("remoteUrl", "http://maven.aliyun.com/nexus/content/groups/public/");
                proxyJSon.put("contentMaxAge", -1);
                proxyJSon.put("metadataMaxAge", 1440);

                JSONObject paramets = new JSONObject();
                paramets.put("name", "proxy-maven");
                paramets.put("online", true);
                paramets.put("storage", storageJSon);
                paramets.put("cleanup", null);
                paramets.put("maven", mavenJSon);
                paramets.put("negativeCache", negativeCacheJSon);
                paramets.put("httpClient", httpClientJSon);
                paramets.put("routingRuleName", null);
                paramets.put("proxy", proxyJSon);

                HttpEntity<String> request =
                        new HttpEntity<>(paramets.toString(), this.headers);

                ResponseEntity<String> responseEntityStr = restTemplate.
                        postForEntity(url, request, String.class);

                if (responseEntityStr.getStatusCodeValue() != 201) {
                    throw new Exception("创建proxy失败!");
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return this;
    }

    /**
     * 初始化中央仓库proxy
     * @return
     * @throws Exception
     */
    public NexusContor addDefaultProxy() throws Exception {
        try {
            if (!this.repositoriesLists.contains("maven-def")) {
                RestTemplate restTemplate = new RestTemplate();
                this.headers.setContentType(MediaType.APPLICATION_JSON);
                //接口地址
                String url;
                if (this.https){
                    url = "https://%s/service/rest/v1/repositories/maven/proxy";
                }else{
                    url = "http://%s/service/rest/v1/repositories/maven/proxy";
                }
                url = String.format(url, this.baseUrl);

                JSONObject storageJSon = new JSONObject();
                storageJSon.put("blobStoreName", "maven-use");
                storageJSon.put("strictContentTypeValidation", false);

                JSONObject mavenJSon = new JSONObject();
                mavenJSon.put("versionPolicy", "RELEASE");
                mavenJSon.put("layoutPolicy", "STRICT");
                mavenJSon.put("contentDisposition", "INLINE");

                JSONObject negativeCacheJSon = new JSONObject();
                negativeCacheJSon.put("enabled", false);
                negativeCacheJSon.put("timeToLive", 1440);

                JSONObject connectionJSon = new JSONObject();
                connectionJSon.put("retries", null);
                connectionJSon.put("userAgentSuffix", null);
                connectionJSon.put("timeout", null);
                connectionJSon.put("enableCircularRedirects", false);
                connectionJSon.put("enableCookies", false);
                connectionJSon.put("useTrustStore", false);


                JSONObject httpClientJSon = new JSONObject();
                httpClientJSon.put("blocked", false);
                httpClientJSon.put("autoBlock", true);
                httpClientJSon.put("connection", connectionJSon);
                httpClientJSon.put("authentication", null);

                JSONObject proxyJSon = new JSONObject();
                proxyJSon.put("remoteUrl", "https://repository.apache.org/content/repositories/snapshots/");
                proxyJSon.put("contentMaxAge", -1);
                proxyJSon.put("metadataMaxAge", 1440);

                JSONObject paramets = new JSONObject();
                paramets.put("name", "maven-def");
                paramets.put("online", true);
                paramets.put("storage", storageJSon);
                paramets.put("cleanup", null);
                paramets.put("maven", mavenJSon);
                paramets.put("negativeCache", negativeCacheJSon);
                paramets.put("httpClient", httpClientJSon);
                paramets.put("routingRule", null);
                paramets.put("proxy", proxyJSon);

                HttpEntity<String> request =
                        new HttpEntity<>(paramets.toString(), this.headers);

                ResponseEntity<String> responseEntityStr = restTemplate.
                        postForEntity(url, request, String.class);

                if (responseEntityStr.getStatusCodeValue() != 201) {
                    throw new Exception("创建中央仓库proxy失败!");
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return this;
    }

    /**
     * 初始化分组
     * @return
     * @throws Exception
     */
    public NexusContor addGroup() throws Exception {
        try {
            if (!this.repositoriesLists.contains("group-maven")) {
                RestTemplate restTemplate = new RestTemplate();
                this.headers.setContentType(MediaType.APPLICATION_JSON);
                //接口地址
                String url;
                if (this.https){
                    url = "https://%s/service/rest/v1/repositories/maven/group";
                }else{
                    url = "http://%s/service/rest/v1/repositories/maven/group";
                }
                url = String.format(url, this.baseUrl);

                JSONObject storageJSon = new JSONObject();
                storageJSon.put("blobStoreName", "maven-use");
                storageJSon.put("strictContentTypeValidation", true);

                ArrayList<String> memberNames = new ArrayList<>();
                memberNames.add("proxy-maven");
                memberNames.add("maven-local");
                memberNames.add("maven-def");
                memberNames.add("snapshots");
                memberNames.add("spring-milestones");
                memberNames.add("spring-snapshots");

                JSONObject groupJSon = new JSONObject();
                groupJSon.put("memberNames", memberNames);

                JSONObject paramets = new JSONObject();
                paramets.put("name", "group-maven");
                paramets.put("online", true);
                paramets.put("storage", storageJSon);
                paramets.put("format", "maven2");
                paramets.put("group", groupJSon);
                paramets.put("type", "group");

                HttpEntity<String> request =
                        new HttpEntity<>(paramets.toString(), this.headers);

                ResponseEntity<String> responseEntityStr = restTemplate.
                        postForEntity(url, request, String.class);

                if (responseEntityStr.getStatusCodeValue() != 201) {
                    throw new Exception("创建分组失败!");
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return this;
    }

    /**
     * 初始化docker仓库
     * @return
     * @throws Exception
     */
    public NexusContor addDocker() throws Exception {
        try {
            if (!this.repositoriesLists.contains("docker-store")) {
                RestTemplate restTemplate = new RestTemplate();
                this.headers.setContentType(MediaType.APPLICATION_JSON);
                //接口地址
                String url;
                if (this.https){
                    url = "https://%s/service/rest/v1/repositories/docker/hosted";
                }else{
                    url = "http://%s/service/rest/v1/repositories/docker/hosted";
                }
                url = String.format(url, this.baseUrl);

                JSONObject storageJSon = new JSONObject();
                storageJSon.put("blobStoreName", "default");
                storageJSon.put("strictContentTypeValidation", true);
                storageJSon.put("writePolicy", "ALLOW");

                JSONObject componentJSon = new JSONObject();
                componentJSon.put("proprietaryComponents", true);

                JSONObject dockerJSon = new JSONObject();
                dockerJSon.put("v1Enabled", false);
                dockerJSon.put("forceBasicAuth", true);
                dockerJSon.put("httpPort", 5000);
                dockerJSon.put("httpsPort", null);

                JSONObject paramets = new JSONObject();
                paramets.put("name", "docker-store");
                paramets.put("online", true);
                paramets.put("storage", storageJSon);
                paramets.put("format", "docker");
                paramets.put("type", "hosted");
                paramets.put("cleanup", null);
                paramets.put("component", componentJSon);
                paramets.put("docker", dockerJSon);

                HttpEntity<String> request =
                        new HttpEntity<>(paramets.toString(), this.headers);

                ResponseEntity<String> responseEntityStr = restTemplate.
                        postForEntity(url, request, String.class);

                if (responseEntityStr.getStatusCodeValue() != 201) {
                    throw new Exception("创建docker仓库失败!");
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return this;
    }

    /**
     * 初始化Snapshot proxy
     * @return
     * @throws Exception
     */
    public NexusContor addSnapshotsProxy() throws Exception {
        try {
            if (!this.repositoriesLists.contains("snapshots")) {
                RestTemplate restTemplate = new RestTemplate();
                this.headers.setContentType(MediaType.APPLICATION_JSON);
                //接口地址
                String url;
                if (this.https){
                    url = "https://%s/service/rest/v1/repositories/maven/proxy";
                }else{
                    url = "http://%s/service/rest/v1/repositories/maven/proxy";
                }
                url = String.format(url, this.baseUrl);

                JSONObject storageJSon = new JSONObject();
                storageJSon.put("blobStoreName", "maven-use");
                storageJSon.put("strictContentTypeValidation", true);
                storageJSon.put("writePolicy", "ALLOW");

                JSONObject mavenJSon = new JSONObject();
                mavenJSon.put("versionPolicy", "RELEASE");
                mavenJSon.put("layoutPolicy", "STRICT");
                mavenJSon.put("contentDisposition", "INLINE");

                JSONObject negativeCacheJSon = new JSONObject();
                negativeCacheJSon.put("enabled", true);
                negativeCacheJSon.put("timeToLive", 1440);

                JSONObject httpClientJSon = new JSONObject();
                httpClientJSon.put("blocked", false);
                httpClientJSon.put("autoBlock", true);
                httpClientJSon.put("connection", null);
                httpClientJSon.put("authentication", null);

                JSONObject proxyJSon = new JSONObject();
                proxyJSon.put("remoteUrl", "https://maven.aliyun.com/repository/snapshots");
                proxyJSon.put("contentMaxAge", -1);
                proxyJSon.put("metadataMaxAge", 1440);

                JSONObject paramets = new JSONObject();
                paramets.put("name", "snapshots");
                paramets.put("online", true);
                paramets.put("storage", storageJSon);
                paramets.put("cleanup", null);
                paramets.put("maven", mavenJSon);
                paramets.put("negativeCache", negativeCacheJSon);
                paramets.put("httpClient", httpClientJSon);
                paramets.put("routingRuleName", null);
                paramets.put("proxy", proxyJSon);

                HttpEntity<String> request =
                        new HttpEntity<>(paramets.toString(), this.headers);

                ResponseEntity<String> responseEntityStr = restTemplate.
                        postForEntity(url, request, String.class);

                if (responseEntityStr.getStatusCodeValue() != 201) {
                    throw new Exception("创建Snapshot proxy失败!");
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return this;
    }

    /**
     * 初始化Spring Milestones proxy
     * @return
     * @throws Exception
     */
    public NexusContor addSprintMilestonesProxy() throws Exception {
        try {
            if (!this.repositoriesLists.contains("spring-milestones")) {
                RestTemplate restTemplate = new RestTemplate();
                this.headers.setContentType(MediaType.APPLICATION_JSON);
                //接口地址
                String url;
                if (this.https){
                    url = "https://%s/service/rest/v1/repositories/maven/proxy";
                }else{
                    url = "http://%s/service/rest/v1/repositories/maven/proxy";
                }

                url = String.format(url, this.baseUrl);

                JSONObject storageJSon = new JSONObject();
                storageJSon.put("blobStoreName", "maven-use");
                storageJSon.put("strictContentTypeValidation", true);
                storageJSon.put("writePolicy", "ALLOW");

                JSONObject mavenJSon = new JSONObject();
                mavenJSon.put("versionPolicy", "RELEASE");
                mavenJSon.put("layoutPolicy", "STRICT");
                mavenJSon.put("contentDisposition", "INLINE");

                JSONObject negativeCacheJSon = new JSONObject();
                negativeCacheJSon.put("enabled", true);
                negativeCacheJSon.put("timeToLive", 1440);

                JSONObject httpClientJSon = new JSONObject();
                httpClientJSon.put("blocked", false);
                httpClientJSon.put("autoBlock", true);
                httpClientJSon.put("connection", null);
                httpClientJSon.put("authentication", null);

                JSONObject proxyJSon = new JSONObject();
                proxyJSon.put("remoteUrl", "https://repo.spring.io/milestone");
                proxyJSon.put("contentMaxAge", -1);
                proxyJSon.put("metadataMaxAge", 1440);

                JSONObject paramets = new JSONObject();
                paramets.put("name", "spring-milestones");
                paramets.put("online", true);
                paramets.put("storage", storageJSon);
                paramets.put("cleanup", null);
                paramets.put("maven", mavenJSon);
                paramets.put("negativeCache", negativeCacheJSon);
                paramets.put("httpClient", httpClientJSon);
                paramets.put("routingRuleName", null);
                paramets.put("proxy", proxyJSon);

                HttpEntity<String> request =
                        new HttpEntity<>(paramets.toString(), this.headers);

                ResponseEntity<String> responseEntityStr = restTemplate.
                        postForEntity(url, request, String.class);

                if (responseEntityStr.getStatusCodeValue() != 201) {
                    throw new Exception("创建Spring milestones proxy失败!");
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return this;
    }

    /**
     * 初始化spring Snapshots proxy
     * @return
     * @throws Exception
     */
    public NexusContor addSpringSnapshotsProxy() throws Exception {
        try {
            if (!this.repositoriesLists.contains("spring-snapshots")) {
                RestTemplate restTemplate = new RestTemplate();
                this.headers.setContentType(MediaType.APPLICATION_JSON);
                //接口地址
                String url;
                if (this.https){
                    url = "https://%s/service/rest/v1/repositories/maven/proxy";
                }else{
                    url = "http://%s/service/rest/v1/repositories/maven/proxy";
                }
                url = String.format(url, this.baseUrl);

                JSONObject storageJSon = new JSONObject();
                storageJSon.put("blobStoreName", "maven-use");
                storageJSon.put("strictContentTypeValidation", true);
                storageJSon.put("writePolicy", "ALLOW");

                JSONObject mavenJSon = new JSONObject();
                mavenJSon.put("versionPolicy", "SNAPSHOT");
                mavenJSon.put("layoutPolicy", "STRICT");
                mavenJSon.put("contentDisposition", "INLINE");

                JSONObject negativeCacheJSon = new JSONObject();
                negativeCacheJSon.put("enabled", true);
                negativeCacheJSon.put("timeToLive", 1440);

                JSONObject httpClientJSon = new JSONObject();
                httpClientJSon.put("blocked", false);
                httpClientJSon.put("autoBlock", true);
                httpClientJSon.put("connection", null);
                httpClientJSon.put("authentication", null);

                JSONObject proxyJSon = new JSONObject();
                proxyJSon.put("remoteUrl", "https://repo.spring.io/snapshot");
                proxyJSon.put("contentMaxAge", -1);
                proxyJSon.put("metadataMaxAge", 1440);

                JSONObject paramets = new JSONObject();
                paramets.put("name", "spring-snapshots");
                paramets.put("online", true);
                paramets.put("storage", storageJSon);
                paramets.put("cleanup", null);
                paramets.put("maven", mavenJSon);
                paramets.put("negativeCache", negativeCacheJSon);
                paramets.put("httpClient", httpClientJSon);
                paramets.put("routingRuleName", null);
                paramets.put("proxy", proxyJSon);

                HttpEntity<String> request =
                        new HttpEntity<>(paramets.toString(), this.headers);

                ResponseEntity<String> responseEntityStr = restTemplate.
                        postForEntity(url, request, String.class);

                if (responseEntityStr.getStatusCodeValue() != 201) {
                    throw new Exception("创建Spring Snapshots proxy失败!");
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return this;
    }
}
