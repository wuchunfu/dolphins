package com.clouds.dolphins.base.kubernetes;


import lombok.Data;

@Data
public class KubIntity {
    // nfs的id
    private String nfsId;
    // nfs的host
    private String nfsHost;
    // 集群的地址
    private String apiServer;
    // 集群的token
    private String token;
    // 集群归属
    private String reGion;
    // 集群id
    private String clusterId;
    // 登陆密码
    private String password;
}
