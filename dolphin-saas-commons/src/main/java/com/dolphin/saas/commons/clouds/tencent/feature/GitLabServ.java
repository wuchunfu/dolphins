package com.dolphin.saas.commons.clouds.tencent.feature;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GitLabServ {
    private final String gitUrl;
    private final String userName;
    private final String passWord;
    private String token;

    public GitLabServ(String gitUrl, String userName, String passWord) throws Exception {
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
            log.error("[服务][GitLab]登录异常,信息: {}", e.getMessage());
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
            log.error("[服务][GitLab]获取Token异常,信息: {}", e.getMessage());
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
    @Retryable(value = Exception.class, maxAttempts = 5)
    public Map<String, Integer> createGroup(String GroupName, String GroupDesc) throws Exception {
        Map<String, Integer> results = new HashMap<>();
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
            Map<String, Object> map = (Map) JSON.parse(responseEntity.getBody());

            results.put(GroupName, Integer.parseInt(map.get("id").toString()));

            // 不加入分组创建代码无效
            while (true) {
                try {
                    this.userJoinGroup(2, map.get("id").toString());
                    break;
                } catch (Exception e) {
                    Thread.sleep(3000);
                }
            }
            return results;
        } catch (Exception e) {
            log.error("[服务][GitLab]创建分组异常,信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 获取已经有的Git分组
     *
     * @return
     * @throws Exception
     */
    @Retryable(value = Exception.class, maxAttempts = 5)
    public Map<String, Integer> getGroupNameList() throws Exception {
        Map<String, Integer> results = new HashMap<>();
        try {
            RestTemplate restTemplate = new RestTemplate();
            //跨域访问表头
            HttpHeaders headers = new HttpHeaders();
            headers.add("PRIVATE-TOKEN", this.token);

            //接口地址
            String url = this.gitUrl + "/api/v3/groups";
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<String>(headers),
                    String.class
            );

            //输出接口所返回过来的值
            JSONArray jarray = JSON.parseArray(responseEntity.getBody());

            for (Object o : jarray) {
                Map<String, Object> obj = (Map<String, Object>) o;
                results.put(obj.get("name").toString(), Integer.parseInt(obj.get("id").toString()));
            }
        } catch (Exception e) {
            log.error("[服务][GitLab]获取分组异常,信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return results;
    }

    /**
     * 获取已经有的用户
     *
     * @return
     * @throws Exception
     */
    public ArrayList<String> getUsersList() throws Exception {
        ArrayList<String> results = new ArrayList<>();
        try {
            RestTemplate restTemplate = new RestTemplate();
            //跨域访问表头
            HttpHeaders headers = new HttpHeaders();
            headers.add("PRIVATE-TOKEN", this.token);

            //接口地址
            String url = this.gitUrl + "/api/v3/users";
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<String>(headers),
                    String.class
            );

            //输出接口所返回过来的值
            JSONArray jarray = JSON.parseArray(responseEntity.getBody());

            for (Object o : jarray) {
                Map<String, Object> obj = (Map<String, Object>) o;
                results.add(obj.get("username").toString());
            }
        } catch (Exception e) {
            log.error("[服务][GitLab]获取用户异常,信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
        return results;
    }


    /**
     * 创建工程
     *
     * @param ProjectName 工程名称
     * @param ProjectDesc 工程介绍
     * @param NamespaceId 分组id
     * @return 返回工程id + 工程地址
     */
    @Retryable(value = Exception.class, maxAttempts = 5)
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
     * 获取项目所有的分支
     *
     * @param ProjectId
     * @return
     * @throws Exception
     */
    @Retryable(value = Exception.class, maxAttempts = 5)
    public ArrayList<Map<String, Object>> branchesLists(Long ProjectId) throws Exception {
        ArrayList<Map<String, Object>> results = new ArrayList<>();

        try {
            RestTemplate restTemplate = new RestTemplate();
            //跨域访问表头
            HttpHeaders headers = new HttpHeaders();
            headers.add("PRIVATE-TOKEN", this.token);
            //接口地址
            String url = this.gitUrl + "/api/v3/projects/" + ProjectId.toString() + "/repository/branches";

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<String>(headers),
                    String.class
            );

            //输出接口所返回过来的值
            JSONArray jarray = JSON.parseArray(responseEntity.getBody());
            for (Object o : jarray) {
                Map<String, Object> obj = (Map<String, Object>) o;
                Map<String, Object> items = new HashMap<>();
                items.put("label", obj.get("name").toString());
                items.put("value", obj.get("name").toString());
                Map<String, Object> commitObj = (Map<String, Object>) obj.get("commit");
                if (commitObj.containsKey("id")) {
                    items.put("commitId", commitObj.get("id"));
                    items.put("authorName", commitObj.get("author_name"));
                    items.put("createTime", commitObj.get("created_at"));
                    items.put("message", commitObj.get("message"));
                }
                results.add(items);
            }
        } catch (Exception e) {
            log.error("branchesLists:" + e);
            throw new Exception(e.getMessage());
        }
        return results;
    }

    /**
     * 判断项目是否存在
     *
     * @param ProjectId
     * @return
     */
    public Boolean checkJobsAlive(Long ProjectId) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            //跨域访问表头
            HttpHeaders headers = new HttpHeaders();
            headers.add("PRIVATE-TOKEN", this.token);
            //接口地址
            String url = this.gitUrl + "/api/v3/projects/" + ProjectId.toString();

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<String>(headers),
                    String.class
            );

            //输出接口所返回过来的值
            Map map = (Map) JSON.parse(responseEntity.getBody());
            return map.containsKey("id");
        } catch (Exception e) {
            return false;
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
    @Retryable(value = Exception.class, maxAttempts = 5)
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

    /**
     * 用户加入分组
     *
     * @param userid
     * @param groupId
     * @throws Exception
     */
    public void userJoinGroup(Integer userid, String groupId) throws Exception {
        try {
            RestTemplate restTemplate = new RestTemplate();
            //跨域访问表头
            HttpHeaders headers = new HttpHeaders();
            headers.add("PRIVATE-TOKEN", this.token);

            //接口地址
            String url = this.gitUrl + "/api/v4/groups/" + groupId + "/members";
            MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
            multiValueMap.add("access_level", "50");
            multiValueMap.add("user_id", userid.toString());

            //放入Http传输的数据
            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(multiValueMap, headers);
            //访问接口并获取返回值
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
            //输出接口所返回过来的值
        } catch (Exception e) {
            log.error("userJoinGroup:" + e);
            throw new Exception(e.getMessage());
        }
    }
}
