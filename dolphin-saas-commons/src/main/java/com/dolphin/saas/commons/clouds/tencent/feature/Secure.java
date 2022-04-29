package com.dolphin.saas.commons.clouds.tencent.feature;

import com.dolphin.saas.commons.clouds.ssh.Remote;
import com.jcraft.jsch.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SSH 服务
 */
public class Secure {

    private static final int SSH_CONNECT_TIMEOUT = 30000;

    /**
     * 登陆ssh
     *
     * @param remote 远程密钥
     * @return
     * @throws JSchException
     */
    public Session getSession(Remote remote) throws JSchException {
        JSch jSch = new JSch();
        if (Files.exists(Paths.get(remote.getIdentity()))) {
            jSch.addIdentity(remote.getIdentity(), remote.getPassphrase());
        }
        Session session = jSch.getSession(remote.getUser(), remote.getHost(), remote.getPort());
        session.setPassword(remote.getPassword());
        session.setConfig("StrictHostKeyChecking", "no");
        return session;
    }

    /**
     * 执行命令(单条，以「;」间隔)
     *
     * @param session 当前的session
     * @param command 执行的命令
     * @return 如果返回为空，则证明执行失败
     * @throws JSchException
     */
    public List<String> remoteExecute(Session session, String command) throws JSchException {
        System.out.println(">> " + command);
        List<String> resultLines = new ArrayList<>();
        ChannelExec channel = null;
        try {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            InputStream input = channel.getInputStream();
            channel.connect(SSH_CONNECT_TIMEOUT);
            try {
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(input));
                String inputLine = null;
                while ((inputLine = inputReader.readLine()) != null) {
                    resultLines.add(inputLine);
                }
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (channel != null) {
                try {
                    channel.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println(resultLines);
        return resultLines;
    }


    /**
     * 执行shell命令
     *
     * @param session      当前的会话
     * @param commandLists 执行的命令
     * @return
     * @throws JSchException
     */
    public Map<String, Object> remoteExecShell(Session session, ArrayList<String> commandLists) throws JSchException {
        Pattern pattern = Pattern.compile("jobs:(\\d+)");
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        channel.connect();
        Map<String, Object> results = new HashMap<>();
        try {
            InputStream inputStream = channel.getInputStream();
            OutputStream outputStream = channel.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream);
            for (String commandList : commandLists) {
                printWriter.println(commandList);
                printWriter.println("echo 'jobs:'$?");
            }
            printWriter.println("exit");
            printWriter.flush();
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String msg = null;
            ArrayList<String> execLogs = new ArrayList<>();
            while ((msg = in.readLine()) != null) {
                execLogs.add(msg);
                Matcher m = pattern.matcher(msg);
                if (m.find()) {
                    int code = Integer.parseInt(m.group(1));
                    if (code > 0) {
                        results.put("error", true);
                    }
                }
            }
            results.put("execLog", execLogs);
            in.close();
        } catch (Exception e) {
            results.put("errorMessage", e.getMessage());
        }
        return results;
    }

    /**
     * SSh 登陆执行命令
     *
     * @param remote
     * @param commandLists
     * @return
     */
    public Map<String, Object> sshExecCommand(Remote remote, ArrayList<String> commandLists) throws JSchException {
        Map<String, Object> results;
        try {
            Session session = this.getSession(remote);
            session.connect(SSH_CONNECT_TIMEOUT);
            if (!session.isConnected()) {
                return null;
            }
            results = this.remoteExecShell(session, commandLists);
            session.disconnect();
            System.out.println("EXEC: "+results.toString());
            return results;
        } catch (JSchException e) {
            throw new JSchException(e.getMessage());
        }
    }

}
