package com.dolphin.saas.commons.clouds.tencent;

import com.dolphin.saas.inputs.AlterMessageInputs;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class AlterServPlugin {
    private final HttpHeaders headers;
    private final String baseKey;
    private String baseUrl;

    public AlterServPlugin(String baseKey) throws Exception {
        this.baseKey = baseKey;
        try {
            this.headers = new HttpHeaders();
        } catch (Exception e) {
            log.error("[服务][消息推送功能]Token异常,信息: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 钉钉提醒功能
     * @param alterMessageInputs
     * @throws Exception
     */
    public void DDalterMsg(AlterMessageInputs alterMessageInputs) throws Exception {
        try {
            this.baseUrl = "https://oapi.dingtalk.com/robot/send?access_token=" + this.baseKey;

            RestTemplate restTemplate = new RestTemplate();
            this.headers.setContentType(MediaType.APPLICATION_JSON);

            JSONObject paramets = new JSONObject();
            paramets.put("msgtype", "markdown");

            String Module, EnvironMent = "";
            if (alterMessageInputs.getReleaseModule() != null) {
                Module = alterMessageInputs.getReleaseJob() + "/" + alterMessageInputs.getReleaseModule();
            } else {
                Module = alterMessageInputs.getReleaseJob();
            }

            switch (alterMessageInputs.getReleaseEnvInfo()) {
                case "test":
                    EnvironMent = alterMessageInputs.getReleaseEnvInfo() + "/测试环境";
                    break;

                case "online":
                    EnvironMent = alterMessageInputs.getReleaseEnvInfo() + "/生产环境";
                    break;

                case "dev":
                    EnvironMent = alterMessageInputs.getReleaseEnvInfo() + "/开发环境";
                    break;

                case "demos":
                    EnvironMent = alterMessageInputs.getReleaseEnvInfo() + "/验收环境";
                    break;
            }

            JSONObject content = new JSONObject();
            content.put("title", "[元豚小助手] 同事们好，于" + alterMessageInputs.getReleaseTime() + "对应用服务进行一次发布，请相关同事注意。");
            content.put("text", "[元豚小助手] 同事们好，于**" + alterMessageInputs.getReleaseTime() + "**对应用服务进行一次发布，请相关同事注意。\n" +
                    "- **应用/模块名：** " + Module + "</font>\n" +
                    "- **发布版本号：**" + alterMessageInputs.getReleaseVersion() + "\n" +
                    "- **发布综合风险：** <font color=#FF0000>" + alterMessageInputs.getReleaseRisk() + "</font>\n" +
                    "- **发布环境：**" + EnvironMent + "\n" +
                    "- **元豚建议：** <font color=#FF0000>" + alterMessageInputs.getReleaseRiskInfo() + "</font>");

            paramets.put("markdown", content);

            HttpEntity<String> request =
                    new HttpEntity<>(paramets.toString(), this.headers);

            ResponseEntity<String> responseEntityStr = restTemplate.
                    postForEntity(this.baseUrl, request, String.class);

            if (responseEntityStr.getStatusCodeValue() != 200) {
                throw new Exception("发送通知失败!");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 微信提醒
     * @param alterMessageInputs
     * @throws Exception
     */
    public void WXalterMsg(AlterMessageInputs alterMessageInputs) throws Exception {
        try {
            this.baseUrl = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=" + this.baseKey;

            RestTemplate restTemplate = new RestTemplate();
            this.headers.setContentType(MediaType.APPLICATION_JSON);

            JSONObject paramets = new JSONObject();
            paramets.put("msgtype", "markdown");

            String Module, EnvironMent = "";
            if (alterMessageInputs.getReleaseModule() != null) {
                Module = alterMessageInputs.getReleaseJob() + "/" + alterMessageInputs.getReleaseModule();
            } else {
                Module = alterMessageInputs.getReleaseJob();
            }

            switch (alterMessageInputs.getReleaseEnvInfo()) {
                case "test":
                    EnvironMent = alterMessageInputs.getReleaseEnvInfo() + "/测试环境";
                    break;

                case "online":
                    EnvironMent = alterMessageInputs.getReleaseEnvInfo() + "/生产环境";
                    break;

                case "dev":
                    EnvironMent = alterMessageInputs.getReleaseEnvInfo() + "/开发环境";
                    break;

                case "demos":
                    EnvironMent = alterMessageInputs.getReleaseEnvInfo() + "/验收环境";
                    break;
            }

            JSONObject content = new JSONObject();
            content.put("content", "[元豚小助手] 同事们好，于" + alterMessageInputs.getReleaseTime() + "对应用服务进行一次发布，请相关同事注意。\n" +
                    ">应用/模块名:<font color=\"comment\">" + Module + "</font>" +
                    "\n>发布版本号:<font color=\"comment\">" + alterMessageInputs.getReleaseVersion() + "</font>" +
                    "\n>发布综合风险:<font color=\"warning\">" + alterMessageInputs.getReleaseRisk() + "</font>" +
                    "\n>发布环境:<font color=\"red\">" + EnvironMent + "</font>" +
                    "\n>元豚建议:<font color=\"red\">" + alterMessageInputs.getReleaseRiskInfo() + "</font>");

            paramets.put("markdown", content);

            HttpEntity<String> request =
                    new HttpEntity<>(paramets.toString(), this.headers);

            ResponseEntity<String> responseEntityStr = restTemplate.
                    postForEntity(this.baseUrl, request, String.class);

            if (responseEntityStr.getStatusCodeValue() != 200) {
                throw new Exception("发送通知失败!");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

}
