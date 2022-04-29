package com.clouds.dolphins.base.gitlab;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GitLabV2Api {
    private final String gitUrl;
    private final String userName;
    private final String passWord;
    private String token;

    public GitLabV2Api(String gitUrl, String userName, String passWord) throws Exception {
        this.gitUrl = gitUrl;
        this.userName = userName;
        this.passWord = passWord;
        this.token = null;
        try {
            RestTemplate restTemplate = new RestTemplate();
            //跨域访问表头
            HttpHeaders headers = new HttpHeaders();
            //接口地址
            String url = this.gitUrl + "/api/v3/session";
            MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
            //利用multiValueMap插入需要传输的数据
            multiValueMap.add("login", this.userName);
            multiValueMap.add("password", this.passWord);
            //放入Http传输的数据
            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(multiValueMap, headers);
            //访问接口并获取返回值
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
            //输出接口所返回过来的值
            Map<String, Object> map = (Map) JSON.parse(responseEntity.getBody());
            this.token = map.get("private_token").toString();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 获取登陆后的token
     *
     * @return
     * @throws Exception
     */
    public String getToken() throws Exception {
        try {
            return this.token;
        } catch (Exception e) {
            log.error("getToken:" + e);
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 创建分组
     *
     * @param GroupName 分组名
     * @param GroupDesc 分组说明
     * @return 返回分组id，不小于0
     */
    public void createGroup(String GroupName, String GroupDesc) throws Exception {
        try {
            RestTemplate restTemplate = new RestTemplate();
            //跨域访问表头
            HttpHeaders headers = new HttpHeaders();
            headers.add("PRIVATE-TOKEN", this.token);

            //接口地址
            String url = this.gitUrl + "/api/v4/groups";
            MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();

            multiValueMap.add("name", GroupName);
            multiValueMap.add("description", GroupDesc);
            multiValueMap.add("path", GroupName);
            multiValueMap.add("visibility", "internal");

            //放入Http传输的数据
            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(multiValueMap, headers);
            //访问接口并获取返回值
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
            //输出接口所返回过来的值
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 创建工程
     *
     * @param ProjectName 工程名称
     * @param ProjectDesc 工程介绍
     * @param NamespaceId 分组id
     * @return 返回工程id + 工程地址
     */
    public Map<String, Object> createProject(String ProjectName, String ProjectDesc, Integer NamespaceId) throws Exception {
        Map<String, Object> results = new HashMap<>();
        try {
            RestTemplate restTemplate = new RestTemplate();
            //跨域访问表头
            HttpHeaders headers = new HttpHeaders();
            headers.add("PRIVATE-TOKEN", this.token);

            //接口地址
            String url = this.gitUrl + "/api/v4/projects";
            MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
            multiValueMap.add("name", ProjectName);
            multiValueMap.add("description", ProjectDesc);
            multiValueMap.add("namespace_id", NamespaceId.toString());

            //放入Http传输的数据
            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(multiValueMap, headers);
            //访问接口并获取返回值
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
            //输出接口所返回过来的值
            Map<String, Object> map = (Map) JSON.parse(responseEntity.getBody());

            results.put("id", map.get("id").toString());
            results.put("url", map.get("http_url_to_repo").toString());
            return results;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 创建一个管理员账户
     *
     * @param uName
     * @param pWord
     * @param email
     * @throws Exception
     */
    public void createUser(String uName, String pWord, String email) throws Exception {
        try {
            RestTemplate restTemplate = new RestTemplate();
            //跨域访问表头
            HttpHeaders headers = new HttpHeaders();
            headers.add("PRIVATE-TOKEN", this.token);

            //接口地址
            String url = this.gitUrl + "/api/v4/users";
            MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
            multiValueMap.add("username", uName);
            multiValueMap.add("password", pWord);
            multiValueMap.add("name", uName);
            multiValueMap.add("skip_confirmation", "true");
            multiValueMap.add("email", email);
            multiValueMap.add("admin", "true");

            //放入Http传输的数据
            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(multiValueMap, headers);
            //访问接口并获取返回值
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
            //输出接口所返回过来的值
        } catch (Exception e) {
            log.error("createUser:" + e);
            throw new Exception(e.getMessage());
        }
    }
}
