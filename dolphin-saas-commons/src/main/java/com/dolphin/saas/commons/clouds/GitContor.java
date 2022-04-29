package com.dolphin.saas.commons.clouds;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GitContor {
    private final UsernamePasswordCredentialsProvider userAuth;
    private File gitFolder;
    private final String FilePath;

    public GitContor(String UserName, String PassWord) throws Exception {
        try {
            // 设置路径
            File Folder = new File("/codes");
            if (!Folder.exists()) {
                this.FilePath = "codes";
            } else {
                this.FilePath = "/codes";
            }
            this.userAuth = new UsernamePasswordCredentialsProvider(UserName, PassWord);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 获取工程最后一次提交的commitID，例子：3008d71bd3587f82b561305e2f20c2c4eed64db4
     * 用于判断是否已经采集过了
     *
     * @param Addr 仓库地址
     * @return
     */
    public String getRemoteCommitId(String Addr) {
        try {
            Collection<Ref> refList = Git.lsRemoteRepository().setRemote(Addr).setCredentialsProvider(this.userAuth).call();
            for (Ref ref : refList) {
                if (ref.getName().startsWith("refs/heads/master")) {
                    return ref.getObjectId().getName();
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }


    /**
     * 克隆工程分析
     *
     * @param JobName
     * @param Addr
     * @throws Exception
     */
    public Map<String, Object> cloneJobs(String JobName, String Addr) throws Exception {
        Map<String, Object> results = new HashMap<>();
        try {
            this.gitFolder = new File(this.FilePath + "/" + JobName);
            Git git = Git.cloneRepository()
                    .setURI(Addr)
                    .setCredentialsProvider(this.userAuth)
                    .setDirectory(this.gitFolder)
                    .setBranch("master")
                    .setMirror(false)
                    .call();
            // 获取分析代码统计结果
            results.put("analysis", this.AnalysisCatalog(git.getRepository().getWorkTree().toString()));
            results.put("commidId", git.getRepository().findRef("refs/heads/master").getObjectId().getName());
            git.close();
            // 删除目录
            FileUtils.deleteDirectory(this.gitFolder);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return results;
    }

    /**
     * 获取分析的结果
     *
     * @param Path 路径
     * @return
     * @throws Exception
     */
    public ArrayList<Map<String, Object>> AnalysisCatalog(String Path) throws Exception {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        try {
            String desc = execCmd("cloc --quiet --csv " + Path, null);
            String[] fileContent = desc.strip().split("\n");

            for (String Line : fileContent) {
                Map<String, Object> items = new HashMap<>();
                // 第一行忽略
                if (Line.startsWith("files")) {
                    continue;
                }
                String[] fileStatistics = Line.split(",");
                // 必须等于5才是正确的，不然就是有空行，这个程序有坑
                if (fileStatistics.length != 5) {
                    continue;
                }
                // 忽略总统计
                if (fileStatistics[1].equals("SUM")) {
                    continue;
                }
                items.put("name", fileStatistics[1]);
                items.put("count", fileStatistics[0]);
                items.put("line", fileStatistics[4]);
                results.add(items);
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return results;
    }


    /**
     * 执行系统命令, 返回执行结果
     *
     * @param cmd 需要执行的命令
     * @param dir 执行命令的子进程的工作目录, null 表示和当前主进程工作目录相同
     */
    public static String execCmd(String cmd, File dir) throws Exception {
        StringBuilder result = new StringBuilder();

        Process process = null;
        BufferedReader bufrIn = null;
        BufferedReader bufrError = null;

        try {
            // 执行命令, 返回一个子进程对象（命令在子进程中执行）
            process = Runtime.getRuntime().exec(cmd, null, dir);

            // 方法阻塞, 等待命令执行完成（成功会返回0）
            process.waitFor();

            // 获取命令执行结果, 有两个结果: 正常的输出 和 错误的输出（PS: 子进程的输出就是主进程的输入）
            bufrIn = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            bufrError = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));

            // 读取输出
            String line = null;
            while ((line = bufrIn.readLine()) != null) {
                result.append(line).append('\n');
            }
            while ((line = bufrError.readLine()) != null) {
                result.append(line).append('\n');
            }

        } finally {
            closeStream(bufrIn);
            closeStream(bufrError);

            // 销毁子进程
            if (process != null) {
                process.destroy();
            }
        }

        // 返回执行结果
        return result.toString();
    }

    private static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (Exception e) {
                // nothing
            }
        }
    }
}
