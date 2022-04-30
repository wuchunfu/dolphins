# Dolphins-DevOPS

<p align="center">
元豚科技-DevOPS-K8S服务自动化部署平台-云上业务搭建<br>
</p>


**主要特性**
- 云配置自动化: 基于Java定时任务接平台任务，自动化部署K8S托管集群，并打通K8S Client，构建容器周边的服务（Jenkins、Sonar、堡垒机、容器安全检测、漏洞扫描等）
- 服务配置打通：CI/CD逻辑的打通，平台可以进行业务自动化发布。
- 部署支付：打通微信支付逻辑。

**目标**
- 满足云上基础服务的配置，如VPC、网络、CFS之类的基础服务。
- 创建K8S集群门槛降低，让人人都能用K8S集群。
- 满足自动化部署的需求。
- 满足业务部署的需求，安全基础服务的部署能力。

**开发技术栈**
- 后端：Java Spring boot、模块化
- 工具端：Python、Golang
- 前端：Vue
- 服务部署：基于K8S容器化部署

### DEMO

主界面:

<img style="max-width:100%;" title="Run example" alt="Run example" src="/pic/1651228541055.jpg">

工程分析页面:

<img style="max-width:100%;" title="Run example" alt="Run example" src="/pic/1651228675842.jpg">

发布页面:

<img style="max-width:100%;" title="Run example" alt="Run example" src="/pic/1651228735417.jpg">

### 备注

- 这个版本并不完善，但是基本的部署、发布跑通了，还需要继续优化迭代。
- 每次阿里云、腾讯云改变规则时，创建集群会有一定概率失败，腾讯云百分百成功，阿里云失败概率极高。
- 欢迎各位找元豚科技做等保二级、三级的服务，元豚科技可以辅助企业搭建基础服务，您无需了解这些代码，只要找我们做等保二级、三级，我们免费为您搭建基础服务并配置好交付。
- 欢迎共同维护和迭代产品，谢谢。

**感谢以下同学**
- 前端：刘权同学
- 工具开发：张春杨同学
- UI设计：鲁佳佳同学

### 加我

<img style="width:200px" title="Run example" alt="Run example" src="/wechat.png">
