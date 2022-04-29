package com.dolphin.saas.commons.clouds.ssh;

import lombok.Data;

@Data
public class Remote {
    private String user = "root";
    private String host = "127.0.0.1";
    private int port = 22;
    private String password = "";
    private String identity = "~/.ssh/id_rsa";
    private String passphrase = "";
}
