import { get, post } from "./request.js";
/*********************************** 基础服务 ***********************************/
//  获取验证码
export const getCodeImg = params => post("/base/captcha", params);
//首页大盘
export const dashboardHeader = params => post("/dashboard/headerCounts", params);
//首页大盘 发布统计
export const dashboardBottom = params => post("/dashboard/bottomCounts", params);
//新手引导、指引的进度条
export const newComerTask = params => post("/dashboard/newComerTask", params);
/*********************************** 用户管理 ***********************************/
// 用户登录
export const login = params => post("/login/in", params);
// 用户注册
export const register = params => post("/login/register", params);
//获取注册短信验证码
export const getCode = params => post('/login/codes', params);
// 用户详情信息
export const getUserInfo = params => post("/users/getUserInfo", params);
// 用户登出
export const logout = params => post("/base/logout", params);
//获取归属组织的用户
export const getUserOptions = params => post("/users/userOptions", params);
//获取用户的列表
export const getUserList = params => post("/users/userLists", params);
//创建用户
export const createUser = params => post("/users/create", params);
//修改用户
export const updateUser = params => post("/users/update", params);
//删除用户
export const deleteUser = params => post("/users/delete", params);




//是否完善信息
export const perfectInfo = params => post("/users/perfectInfo", params);
//个人用户-引导信息提交
export const guideInfoUsers = params => post("/users/guideInfoUsers", params);
//企业用户-引导信息提交
export const guideInfoCompany = params => post("/users/guideInfoCompany", params);
//商户模糊搜索接口
export const getMerchantName = params => post("/merchant/getMerchantName", params);
/*********************************** 云厂商配置 ***********************************/
//云厂商下拉列表
export const getCloudLists = params => get("/ventors/ventorTypeOptions", params);
//云厂商的地域列表
export const getCloudRegions = params => post("/ventors/ventorRegionsOptions", params);
//云厂商的地域下的可用区列表
export const getCloudRegionZones = params => post("/ventors/ventorRegionsZoneOptions", params);
//判断是否已经有厂商了
export const judgmentCloud = params => get("/ventors/judgment", params);
//创建云厂商配置
export const createCloud = params => post("/ventors/create", params);
//云厂商配置列表
export const getCloudlist = params => post("/ventors/lists", params);
//云厂商单条数据
export const getCloudInfo = params => post("/ventors/read", params);
//更新云厂商信息
export const updateCloud = params => post("/ventors/change", params);
//删除云厂商ak
export const delCloud = params => post("/ventors/ventorDelete", params);

/******** 云上资产部署 ***********/
//判断是否有资产
export const judgmentAsset = params => get("/cvm/judgment", params);
//所有的云资产列表
export const assetlists = params => post("/cvm/lists", params);
//更新云资产数据
export const updateAsset = params => get("/cvm/updateCvmJobs", params);
//所有可以打的服务标签下拉
export const getTagSelect = params => get("/cvm/getTags", params);
//查看单条云资产信息
export const getAssetInfo = params => post("/cvm/read", params);
//打标签执行部署任务
export const buildTag = params => post("/cvm/buildTag", params);
//检查是否有正在部署中的资产
export const checkAsset = params => get("/cvm/checkCvmJobs", params);

/*********************************** 集群搭建 ***********************************/
//判断是否已有集群
export const judgmentCluster = params => get("/cluster/judgment", params);
//创建集群询价&代决策
export const createClusterCalculate = params => post("/cluster/createClusterCalculate", params);
//下单创建集群
export const createClusterOrders = params => post("/cluster/createClusterOrders", params);
//支付回调
export const payCallback = params => post('/cluster/checkOrders', params)
//集群列表
export const clusterlists = params => post("/cluster/lists", params);
//获取集群信息
export const getClusterInfo = params => post("/cluster/read", params);
//获取部署阶段记录
export const getDeployStages = params => post("/cluster/readStages", params);
//获取集群部署的资产资源
export const getClusterAssets = params => post("/cluster/readClusterAssets", params);
//执行部署的逻辑
export const execClusterDeploy = params => post("/cluster/execClusterDeploy", params);
//获取负载的所有pod KuberNameSpaceDeploymentPods
export const getAllPods = params => get("/weaveplatform/getNamespaceDeploymentPods", params);
//获取命名空间下的所有Deployment
export const getAllDeployments = params => get("/weaveplatform/getNamespaceDeployments", params);
//获取所有命名空间KuberNameSpace
export const getNamespaces = params => get("/weaveplatform/getNamespaces", params);
//回收集群
export const recycleClusterDeploy = params => post("/cluster/recycleClusterDeploy", params);
//集群的部署信息读取
export const readClusterMessage = params => post("/cluster/readClusterMessage", params)

/*********************************** 业务发布 ***********************************/
//已创建的云厂商下拉列表
export const getEngineerCloudLists = params => get("/engineer/ventorTypeOptions", params);

/******** 发布策略 ***********/
//判断是否有发布策略
export const judgmentRulesGroup = params => get("/rulesgroup/judgment", params);
//获取发布组列表
export const rulesGroupList = params => post("/rulesgroup/lists", params);
//修改发布策略组
export const editRulesGroup = params => post("/rulesgroup/change", params);
//创建发布策略组
export const createRulesGroup = params => post("/rulesgroup/create", params);
//删除发布策略组
export const deleteRulesGroup = params => post("/rulesgroup/delete", params);
//发布策略选项集合
export const rulesList = params => get("/rulesgroup/rules", params);


/******** 工程管理 ***********/
//判断工程是否存在
export const judgmentEngineer = params => get("/engineer/judgment", params);
//创建标准工程
export const createEngineer = params => post("/engineer/create", params);
//项目工程列表
export const engineerList = params => post("/engineer/lists", params);
//获取工程信息
export const getEngineerInfo = params => post("/engineer/read", params);
//获取dockerfile的下拉信息
export const getDockerfileSelect = params => post("/engineer/dockerfileOptions", params);
//获取发布策略组
export const getRulesGroupSelect = params => get("/engineer/releaserulesOptions", params);
//获取开发语言
export const getLanguageSelect = params => get("/engineer/LanguageOptions", params);
//获取开发框架
export const getFrameWorkSelect = params => post("/engineer/FrameWorkOptions", params);
//获取gitLab的nameSpace
export const getGitNamespaceSelect = params => post("/engineer/gitlabNamespaceOptions", params);
//获取工程发布列表
export const getEngineerReleaseList = params => post("/engineer/releaseLists", params)



/******** 发布管理 ***********/
//判断是否有发布项目
export const judgmentRelease = params => get("/release/judgment", params);
//创建任务发布
export const createRelease = params => post("/release/create", params);
//发布任务列表
export const releaseList = params => post("/release/lists", params);
//执行/回滚发布任务
export const releaseExecute = params => post("/release/releaseExecute", params);
//获取发布的信息
export const getReleaseInfo = params => post("/release/read", params);
//获取业务发布阶段
export const getReleaseStages = params => post("/release/releaseStages", params);
//获取回滚的版本列表
export const getRollbackList = params => post("/release/releaseVerOptions", params);
//获取获取发布债务列表
export const getReleaseDetes = params => post("/release/releaseDetes", params);
//获取回滚的版本列表
export const releaseVerOptions = params => post("/release/releaseVerOptions", params);
//根据云厂商获得集群id和信息
export const getCloudClusterList = params => post("/release/cloudClusterId", params);
//根据集群id获取namespace信息
export const getClusterNameSpaceList = params => post("/release/clusterIdNameSpace", params);
//发布工程名称下拉
export const getEngineerSelect = params => post("/release/engineerOptions", params)
//根据项目名查分支列表和MR信息
export const getEngineerBranch = params => post("/release/engineerBranch", params)
