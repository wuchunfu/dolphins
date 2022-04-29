(window.webpackJsonp=window.webpackJsonp||[]).push([["chunk-668af6fc"],{"1f81":function(e,t,r){"use strict";r.r(t);var a=r("5530"),n=(r("d3b7"),r("365c")),i=r("3d73"),s={name:"create",props:{options:{type:Object,required:!0}},data:function(){return{form:this.$form.createForm(this),cloudList:[],languageList:[],frameworkList:[],dockerFileList:[],busGrouplist:[],nameSpacelist:[]}},methods:{init:function(){this.getCloudList(),this.getLanguage(),this.getStrategyGroup()},cloudChange:function(){this.$nextTick((function(){var e=this;getCloudClusterList({cloudTypeId:this.form.getFieldsValue().releaseJobCloudId}).then((function(t){var r=t.status,a=t.data,n=t.msg;1===r?(e.cloudClusterList=a||[],e.form.setFieldsValue({releaseJobClusterId:void 0})):e.$message.error(n)})).catch((function(){}))}))},getCloudList:function(){var e=this;Object(n.E)().then((function(t){var r=t.status,a=t.data,n=t.msg;1===r?e.cloudList=a||[]:e.$message.error(n)})).catch((function(){}))},getLanguage:function(){var e=this;Object(n.K)().then((function(t){var r=t.status,a=t.data,n=t.msg;1===r?e.languageList=Array.isArray(a)?a:[]:e.$message.error(n)})).catch((function(e){}))},frameworkChange:function(){this.$nextTick((function(){var e=this;Object(n.C)({engineerLanguageId:this.form.getFieldsValue().engineerLanguageId,engineerFrameworkId:this.form.getFieldsValue().engineerFrameworkId}).then((function(t){var r=t.status,a=t.data,n=t.msg;1===r?e.dockerFileList=Array.isArray(a)?a:[]:e.$message.error(n)})).catch((function(e){}))}))},getGitNamespaceSelect:function(){this.$nextTick((function(){var e=this;Object(n.J)({ventorId:this.form.getFieldsValue().engineerCloudId}).then((function(t){var r=t.status,a=t.data,n=t.msg;1===r?(e.nameSpacelist=Array.isArray(a)?a:[],e.form.setFieldsValue({engineerGitGroupId:void 0})):e.$message.error(n)})).catch((function(e){}))}))},getStrategyGroup:function(){var e=this;Object(n.Q)().then((function(t){var r=t.status,a=t.data,n=t.msg;1===r?e.busGrouplist=Array.isArray(a)?a:[]:e.$message.error(n)})).catch((function(e){}))},languageChange:function(){this.$nextTick((function(){var e=this;Object(n.I)({LanguageId:this.form.getFieldsValue().engineerLanguageId}).then((function(t){var r=t.status,a=t.data,n=t.msg;1===r?(e.frameworkList=Array.isArray(a)?a:[],e.form.setFieldsValue({engineerFrameworkId:void 0})):e.$message.error(n)})).catch((function(e){}))}))}},created:function(){this.init()}},o=s,l=r("2877"),c=Object(l.a)(o,(function(){var e=this,t=e.$createElement,r=e._self._c||t;return r("div",[r("a-form",{ref:"form",attrs:{form:e.form,"label-col":{span:5},layout:"horizontal","wrapper-col":{span:17}}},[r("a-row",{attrs:{gutter:32,type:"flex",justify:"center"}},[r("a-col",{attrs:{xs:24,sm:12,lg:"enterprise"==e.options.type?12:24}},[r("a-form-item",{attrs:{label:"工程名称",help:"(格式XXX-XXX-XXXX)"}},[r("a-input",{directives:[{name:"decorator",rawName:"v-decorator",value:["engineerName",{rules:[{required:!0,message:"工程名称不能为空!"}]}],expression:"['engineerName', { rules: [{ required: true, message: '工程名称不能为空!' }] }]"}],attrs:{placeholder:"请输入工程名称"}})],1),r("a-form-item",{attrs:{label:"归属云"}},[r("a-select",{directives:[{name:"decorator",rawName:"v-decorator",value:["engineerCloudId",{rules:[{required:!0,message:"归属云不能为空!"}]}],expression:"['engineerCloudId', { rules: [{ required: true, message: '归属云不能为空!' }] }]"}],attrs:{placeholder:"请输入归属云"},on:{change:e.getGitNamespaceSelect}},e._l(e.cloudList,(function(t){return r("a-select-option",{key:t.value,attrs:{value:t.value}},[e._v(e._s(t.label))])})),1)],1),r("a-form-item",{attrs:{label:"开发语言"}},[r("a-select",{directives:[{name:"decorator",rawName:"v-decorator",value:["engineerLanguageId",{rules:[{required:!0,message:"开发语言不可为空!"}]}],expression:"['engineerLanguageId', { rules: [{ required: true, message: '开发语言不可为空!' }] },]"}],attrs:{placeholder:"请选择开发语言"},on:{change:e.languageChange}},e._l(e.languageList,(function(t){return r("a-select-option",{key:t.value},[e._v(e._s(t.label))])})),1)],1),r("a-form-item",{attrs:{label:"开发框架"}},[r("a-select",{directives:[{name:"decorator",rawName:"v-decorator",value:["engineerFrameworkId"],expression:"['engineerFrameworkId']"}],attrs:{placeholder:"请选择开发框架"},on:{change:e.frameworkChange}},e._l(e.frameworkList,(function(t){return r("a-select-option",{key:t.value},[e._v(e._s(t.label))])})),1)],1),r("a-form-item",{attrs:{label:"DockerFile模板"}},[r("a-select",{directives:[{name:"decorator",rawName:"v-decorator",value:["engineerDockerfileId",{rules:[{required:!0,message:"开发框架不可为空!"}]}],expression:"['engineerDockerfileId', { rules: [{ required: true, message: '开发框架不可为空!' }] },]"}],attrs:{placeholder:"请选择DockerFile模板"}},e._l(e.dockerFileList,(function(t){return r("a-select-option",{key:t.value},[e._v(e._s(t.label))])})),1)],1),r("a-form-item",{attrs:{label:"代码分组"}},[r("a-select",{directives:[{name:"decorator",rawName:"v-decorator",value:["engineerGitGroupId",{rules:[{required:!0,message:"代码分组不可为空!"}]}],expression:"['engineerGitGroupId', { rules: [{ required: true, message: '代码分组不可为空!' }] },]"}],attrs:{placeholder:"请选择代码分组"}},e._l(e.nameSpacelist,(function(t){return r("a-select-option",{key:t.value},[e._v(e._s(t.label))])})),1)],1),r("a-form-item",{attrs:{label:"发布策略组","validate-status":"warning",help:"若无'发布策略组'下拉选项，则需先创建发布策略组后，再进行创建工程"}},[r("a-select",{directives:[{name:"decorator",rawName:"v-decorator",value:["engineerReleaseRulesId",{rules:[{required:!0,message:"开发框架不可为空!"}]}],expression:"['engineerReleaseRulesId', { rules: [{ required: true, message: '开发框架不可为空!' }] },]"}],attrs:{placeholder:"请选择归属的业务组"}},e._l(e.busGrouplist,(function(t){return r("a-select-option",{key:t.value},[e._v(e._s(t.label))])})),1)],1),"enterprise"!=e.options.type?r("a-form-item",{attrs:{label:"工程备注"}},[r("a-textarea",{directives:[{name:"decorator",rawName:"v-decorator",value:["engineerRemark"],expression:"['engineerRemark']"}],attrs:{placeholder:"请输入工程备注","auto-size":{minRows:3,maxRows:6}}})],1):e._e()],1),"enterprise"==e.options.type?r("a-col",{attrs:{xs:24,sm:24,lg:11}},[r("a-form-item",{attrs:{label:"开发负责人"}},[r("a-select",{directives:[{name:"decorator",rawName:"v-decorator",value:["engineerCodeing",{rules:[{required:!0,message:"开发负责人不可为空!"}]}],expression:"['engineerCodeing', { rules: [{ required: true, message: '开发负责人不可为空!' }] },]"}],attrs:{placeholder:"请选择开发负责人"}},e._l(e.options.orgUsers,(function(t){return r("a-select-option",{key:t.value},[e._v(e._s(t.label))])})),1)],1),r("a-form-item",{attrs:{label:"安全负责人"}},[r("a-select",{directives:[{name:"decorator",rawName:"v-decorator",value:["engineerSecurity",{rules:[{required:!0,message:"安全负责人不能为空!"}]}],expression:"['engineerSecurity', { rules: [{ required: true, message: '安全负责人不能为空!' }] },]"}],attrs:{placeholder:"请选择安全负责人"}},e._l(e.options.orgUsers,(function(t){return r("a-select-option",{key:t.value},[e._v(e._s(t.label))])})),1)],1),r("a-form-item",{attrs:{label:"运维负责人"}},[r("a-select",{directives:[{name:"decorator",rawName:"v-decorator",value:["engineerDevops",{rules:[{required:!0,message:"运维负责人不能为空!"}]}],expression:"['engineerDevops', { rules: [{ required: true, message: '运维负责人不能为空!' }] },]"}],attrs:{placeholder:"请选择运维负责人"}},e._l(e.options.orgUsers,(function(t){return r("a-select-option",{key:t.value},[e._v(e._s(t.label))])})),1)],1),r("a-form-item",{attrs:{label:"测试负责人"}},[r("a-select",{directives:[{name:"decorator",rawName:"v-decorator",value:["engineerTesting",{rules:[{required:!0,message:"测试负责人不能为空!"}]}],expression:"['engineerTesting', { rules: [{ required: true, message: '测试负责人不能为空!' }] },]"}],attrs:{placeholder:"请选择测试负责人"}},e._l(e.options.orgUsers,(function(t){return r("a-select-option",{key:t.value},[e._v(e._s(t.label))])})),1)],1),r("a-form-item",{attrs:{label:"业务负责人"}},[r("a-select",{directives:[{name:"decorator",rawName:"v-decorator",value:["engineerVocational",{rules:[{required:!0,message:"业务负责人不能为空!"}]}],expression:"['engineerVocational', { rules: [{ required: true, message: '业务负责人不能为空!' }] },]"}],attrs:{placeholder:"请选择业务负责人"}},e._l(e.options.orgUsers,(function(t){return r("a-select-option",{key:t.value},[e._v(e._s(t.label))])})),1)],1),r("a-form-item",{attrs:{label:"工程备注"}},[r("a-textarea",{directives:[{name:"decorator",rawName:"v-decorator",value:["engineerRemark"],expression:"['engineerRemark']"}],attrs:{placeholder:"请输入工程备注","auto-size":{minRows:3,maxRows:6}}})],1)],1):e._e()],1)],1)],1)}),[],!1,null,null,null),u=c.exports,d=(r("a9e3"),r("432b")),g=[{title:"工程ID",dataIndex:"id"},{title:"创建时间",className:"mw120",dataIndex:"createtime"},{title:"云厂商",className:"mw90",dataIndex:"cloudId"},{title:"工程状态",className:"mw90",dataIndex:"status",scopedSlots:{customRender:"status"}},{title:"发布耗时",className:"mw90",dataIndex:"ms",scopedSlots:{customRender:"ms"}}],m={0:{status:"default",text:"待检查"},1:{status:"processing",text:"检查中"},2:{status:"processing",text:"待发布"},3:{status:"processing",text:"发布中"},4:{status:"success",text:"已发布"},5:{status:"warning",text:"回滚中"},6:{status:"warning",text:"已回滚"},7:{status:"default",text:"发布异常"},8:{status:"error",text:"项目异常"}},p={mixins:[d.a],props:{engineerId:{type:Number,required:!0},options:{type:Object,required:!0}},filters:{statusFilter:function(e){return m[e].text},statusTypeFilter:function(e){return m[e].status},formatSeconds:function(e){var t=parseInt(e),r=Math.floor(t/3600)<10?"0"+Math.floor(t/3600):Math.floor(t/3600),a=Math.floor(t/60%60)<10?"0"+Math.floor(t/60%60):Math.floor(t/60%60),n=Math.floor(t%60)<10?"0"+Math.floor(t%60):Math.floor(t%60),i="";return"00"!==r&&(i+="".concat(r,"小时")),"00"!==a&&(i+="".concat(a,"分")),i+="".concat(n,"秒")}},data:function(){return this.columns=g,{engineerInfo:{engineerId:"",engineerRemark:"",engineerDevops:"",engineerUpdatetime:"",engineerTesting:"",engineerGiturl:"",engineerReleaseRules:"",engineerFramework:"",engineerName:"",engineerSecurity:"",engineerStatus:"",engineerCodeing:"",engineerVocational:"",engineerDockerfile:"",engineerLanguage:"",engineerCreatetime:""},releaseList:[],listLoading:!1,pagination:{current:1,pageSize:10,total:0,showQuickJumper:!0,hideOnSinglePage:!0,showTotal:function(e){return"共 ".concat(e," 条数据")}}}},methods:{tableChange:function(e){this.pagination=e,this.getList()},goDetails:function(){var e=this,t=this.engineerId;Object(n.F)({engineerId:t}).then((function(t){var r=t.status,n=t.data,i=t.msg;1===r?e.engineerInfo=Object(a.a)({},n):e.$message.error(i)})).catch((function(e){})),this.getList()},getList:function(){var e=this;this.listLoading=!0;var t=this.pagination,r=t.current,i=t.pageSize;Object(n.G)(Object(a.a)(Object(a.a)({},{page:r,pageSize:i}),{engineerId:this.engineerId})).then((function(t){var r=t.status,a=t.data,n=t.msg;1===r?(e.releaseList=a||[],total&&(e.pagination={current:page,pageSize:i,total:total})):e.$message.error(n),e.listLoading=!1})).catch((function(t){e.listLoading=!1}))}},created:function(){this.goDetails()}},f=p,h=Object(l.a)(f,(function(){var e=this,t=e.$createElement,r=e._self._c||t;return r("div",[r("a-divider",{attrs:{orientation:"left"}},[e._v("工程详情")]),r("a-descriptions",{attrs:{size:"small",column:e.isMobile?1:3,layout:"vertical",bordered:""}},[r("a-descriptions-item",{attrs:{label:"工程名称"}},[e._v(e._s(e.engineerInfo.engineerName))]),r("a-descriptions-item",{attrs:{label:"开发语言"}},[e._v(e._s(e.engineerInfo.engineerLanguage))]),r("a-descriptions-item",{attrs:{label:"开发框架"}},[e._v(e._s(e.engineerInfo.engineerFramework))]),r("a-descriptions-item",{attrs:{label:"业务发布策略组"}},[e._v(e._s(e.engineerInfo.engineerReleaseRules))]),r("a-descriptions-item",{attrs:{label:"创建时间"}},[e._v(e._s(e.engineerInfo.engineerCreatetime))]),r("a-descriptions-item",{attrs:{label:"更新时间"}},[e._v(e._s(e.engineerInfo.engineerUpdatetime||"无"))]),"enterprise"==e.options.type?r("a-descriptions-item",{attrs:{label:"开发负责人"}},[e._v(e._s(e.engineerInfo.engineerCodeing||"未定义"))]):e._e(),"enterprise"==e.options.type?r("a-descriptions-item",{attrs:{label:"运维负责人"}},[e._v(e._s(e.engineerInfo.engineerDevops||"未定义"))]):e._e(),"enterprise"==e.options.type?r("a-descriptions-item",{attrs:{label:"安全负责人"}},[e._v(e._s(e.engineerInfo.engineerSecurity||"未定义"))]):e._e(),"enterprise"==e.options.type?r("a-descriptions-item",{attrs:{label:"测试负责人"}},[e._v(e._s(e.engineerInfo.engineerTesting||"未定义"))]):e._e(),"enterprise"==e.options.type?r("a-descriptions-item",{attrs:{label:"业务负责人"}},[e._v(e._s(e.engineerInfo.engineerVocational||"未定义"))]):e._e(),r("a-descriptions-item",{attrs:{label:"备注信息"}},[e._v(e._s(e.engineerInfo.engineerRemark||"无"))]),r("a-descriptions-item",{attrs:{label:"代码仓库地址"}},[e._v(e._s(e.engineerInfo.engineerGiturl))])],1),r("a-divider",{attrs:{orientation:"left"}},[e._v("发布列表")]),r("a-table",{ref:"table",attrs:{size:"middle",columns:e.columns,dataSource:e.releaseList,loading:e.listLoading,pagination:e.pagination,rowKey:function(e,t){return t}},on:{change:e.tableChange},scopedSlots:e._u([{key:"status",fn:function(t){return r("span",{},[r("a-badge",{attrs:{status:e._f("statusTypeFilter")(t),text:e._f("statusFilter")(t)}})],1)}},{key:"ms",fn:function(t){return r("span",{},[e._v(e._s(e._f("formatSeconds")(t)))])}}])})],1)}),[],!1,null,null,null),v=h.exports,b=[{title:"工程名称",dataIndex:"engineerName",scopedSlots:{customRender:"engineerName"}},{title:"工程状态",dataIndex:"engineerStatus",width:"80px",scopedSlots:{customRender:"status"}},{title:"语言/框架",dataIndex:"engineerLanguageId",scopedSlots:{customRender:"languageAndFramework"}},{title:"业务发布策略组",className:"mw120",dataIndex:"engineerReleaseRulesId"},{title:"创建时间",className:"mw120",dataIndex:"engineerCreatetime"},{title:"更新时间",className:"mw120",dataIndex:"engineerUpdatetime",scopedSlots:{customRender:"updateTime"}},{title:"备注",dataIndex:"engineerRemark",scopedSlots:{customRender:"remark"}},{title:"操作",dataIndex:"action",align:"center",width:"150px",scopedSlots:{customRender:"action"}}],_={0:{status:"processing",text:"待创建"},1:{status:"processing",text:"创建中"},2:{status:"success",text:"已创建"},3:{status:"error",text:"已失效"}},y={mixins:[i.a],components:{CreateForm:u,DetailForm:v},filters:{statusFilter:function(e){return _[e].text},statusTypeFilter:function(e){return _[e].status}},data:function(){return{searchForm:{engineerName:void 0,engineerLanguageId:void 0,engineerFrameworkId:void 0},pagination:{current:1,pageSize:10,total:0,showQuickJumper:!0,hideOnSinglePage:!0,showTotal:function(e){return"共 ".concat(e," 条数据")}},list:[],columns:b,statusList:[{type:"",label:"待初始化"},{type:"success",label:"启用"},{type:"danger",label:"异常"}],listLoading:!1,cloudList:[],languageList:[],frameworkList:[],userOptions:{},loading:!1,createDrawer:!1,detailDrawer:!1,engineerId:0}},methods:{search:function(){this.pagination.current=1,this.getList()},tableChange:function(e){this.pagination=e,this.getList()},getList:function(){var e=this;this.listLoading=!0;var t=this.pagination,r=t.current,i=t.pageSize;Object(n.p)(Object(a.a)(Object(a.a)({},{page:r,pageSize:i}),this.searchForm)).then((function(t){var r=t.status,a=t.data,n=t.msg;if(1===r){var i=a.page,s=a.pageSize,o=a.total,l=a.list;e.list=l||[],e.pagination={current:i,pageSize:s,total:o}}else e.$message.error(n);e.listLoading=!1})).catch((function(t){e.listLoading=!1}))},judgmentEngineer:function(){var e=this,t=this;this.$destroyAll(),Object(n.Z)().then((function(r){var a=r.status;r.data,r.msg,1!=a&&e.$confirm({title:"提示",content:"当前暂未创建项目工程",okText:"去创建",maskClosable:!0,onOk:function(){return t.create(),new Promise((function(e,t){e()})).catch((function(){}))},onCancel:function(){}})})).catch((function(e){}))},goDetails:function(e){this.engineerId=e,this.detailDrawer=!0},create:function(){var e=this;this.createDrawer=!0,Object(n.T)().then((function(t){var r=t.status,a=t.data,n=t.msg;1===r?e.userOptions=a:e.$message.error(n)})).catch((function(e){})),this.$nextTick((function(){this.$refs.createForm.form.resetFields()}))},submit:function(){var e=this;this.loading=!0;(0,this.$refs.createForm.form.validateFields)(["engineerName","engineerDockerfileId","engineerFrameworkId","engineerCloudId","engineerCodeing","engineerDevops","engineerLanguageId","engineerReleaseRulesId","engineerGitGroupId","engineerRemark","engineerSecurity","engineerVocational"],{force:!0},(function(t,r){if(t)return e.loading=!1,!1;Object(n.h)(Object(a.a)({},r)).then((function(t){var r=t.status,a=(t.data,t.msg);1===r?(e.$message.success(a),e.pagination.current=1,e.getList(),e.createDrawer=!1):e.$message.error(a),e.loading=!1})).catch((function(){e.loading=!1}))}))},getCloudList:function(){var e=this;Object(n.t)().then((function(t){var r=t.status,a=t.data,n=t.msg;1===r?e.cloudList=a||[]:e.$message.error(n)})).catch((function(){}))},getDockerfile:function(){var e=this;Object(n.C)().then((function(t){var r=t.status,a=t.data,n=t.msg;1===r?e.dockerFileList=Array.isArray(a)?a:[]:e.$message.error(n)})).catch((function(e){}))},getLanguage:function(){var e=this;Object(n.K)().then((function(t){var r=t.status,a=t.data,n=t.msg;1===r?e.languageList=Array.isArray(a)?a:[]:e.$message.error(n)})).catch((function(e){}))},languageChange:function(){var e=this;Object(n.I)({LanguageId:this.searchForm.engineerLanguageId}).then((function(t){var r=t.status,a=t.data,n=t.msg;1===r?(e.frameworkList=Array.isArray(a)?a:[],e.searchForm.engineerFrameworkId=void 0):e.$message.error(n)})).catch((function(e){}))}},created:function(){this.getCloudList(),this.getList(),this.getLanguage(),this.judgmentEngineer()},mounted:function(){},beforeCreate:function(){},beforeMount:function(){},beforeUpdate:function(){},updated:function(){},beforeDestroy:function(){},destroyed:function(){},activated:function(){}},w=y,I=Object(l.a)(w,(function(){var e=this,t=e.$createElement,r=e._self._c||t;return r("div",[r("a-card",{attrs:{bordered:!1}},[r("a-form",{directives:[{name:"show",rawName:"v-show",value:e.showSearch,expression:"showSearch"}],staticClass:"mb20",attrs:{layout:"inline"},model:{value:e.searchForm,callback:function(t){e.searchForm=t},expression:"searchForm"}},[r("a-form-item",{attrs:{label:"工程名称"}},[r("a-input",{attrs:{placeholder:"请输入工程名称"},nativeOn:{keyup:function(t){return!t.type.indexOf("key")&&e._k(t.keyCode,"enter",13,t.key,"Enter")?null:e.search.apply(null,arguments)}},model:{value:e.searchForm.engineerName,callback:function(t){e.$set(e.searchForm,"engineerName",t)},expression:"searchForm.engineerName"}})],1),r("a-form-item",{attrs:{label:"工程语言"}},[r("a-select",{staticClass:"mw180",attrs:{placeholder:"请选择工程语言"},on:{change:e.languageChange},model:{value:e.searchForm.engineerLanguageId,callback:function(t){e.$set(e.searchForm,"engineerLanguageId",t)},expression:"searchForm.engineerLanguageId"}},e._l(e.languageList,(function(t){return r("a-select-option",{key:t.value,attrs:{value:t.value}},[e._v(e._s(t.label))])})),1)],1),r("a-form-item",{attrs:{label:"工程框架"}},[r("a-select",{staticClass:"mw180",attrs:{placeholder:"请选择工程框架"},model:{value:e.searchForm.engineerFrameworkId,callback:function(t){e.$set(e.searchForm,"engineerFrameworkId",t)},expression:"searchForm.engineerFrameworkId"}},e._l(e.frameworkList,(function(t){return r("a-select-option",{key:t.value,attrs:{value:t.value}},[e._v(e._s(t.label))])})),1)],1),r("a-form-item",{attrs:{label:"归属云"}},[r("a-select",{staticClass:"mw180",attrs:{placeholder:"请选择归属云"},model:{value:e.searchForm.engineerCloudId,callback:function(t){e.$set(e.searchForm,"engineerCloudId",t)},expression:"searchForm.engineerCloudId"}},e._l(e.cloudList,(function(t){return r("a-select-option",{key:t.value,attrs:{value:t.value}},[e._v(e._s(t.label))])})),1)],1),r("a-form-item",[r("a-button",{attrs:{type:"primary"},nativeOn:{click:function(t){return t.preventDefault(),e.search.apply(null,arguments)}}},[e._v("查询")]),r("a-button",{staticStyle:{"margin-left":"8px"},on:{click:function(){return e.searchForm={}}}},[e._v("重置")])],1)],1),r("a-row",{attrs:{gutter:10,type:"flex",justify:"space-between"}},[r("a-col",{attrs:{span:12}},[r("a-button",{attrs:{type:"primary",icon:"plus",ghost:""},on:{click:e.create}},[e._v("创建")])],1),r("a-col",{attrs:{span:2}},[r("right-toolbar",{attrs:{showSearch:e.showSearch},on:{"update:showSearch":function(t){e.showSearch=t},"update:show-search":function(t){e.showSearch=t},queryTable:e.getList}})],1)],1),r("div",{staticClass:"table-operator"}),r("a-table",{ref:"table",attrs:{size:"middle",columns:e.columns,dataSource:e.list,loading:e.listLoading,pagination:e.pagination,rowKey:function(e,t){return t}},on:{change:e.tableChange},scopedSlots:e._u([{key:"engineerName",fn:function(t,a){return r("span",{},[r("a-tooltip",{attrs:{title:"仓库地址: "+a.engineerGiturl,placement:"top","arrow-point-at-center":""}},[r("span",[e._v(e._s(t))])])],1)}},{key:"status",fn:function(t){return r("span",{},[r("a-badge",{attrs:{status:e._f("statusTypeFilter")(t),text:e._f("statusFilter")(t)}})],1)}},{key:"languageAndFramework",fn:function(t,a){return r("span",{},[e._v(" "+e._s(t)+" "),r("br"),e._v(" "+e._s(a.engineerFrameworkId)+" ")])}},{key:"updateTime",fn:function(t){return r("span",{},[r("span",[e._v(e._s(t||"无"))])])}},{key:"remark",fn:function(t){return r("span",{},[r("span",[e._v(e._s(t||"无"))])])}},{key:"action",fn:function(t,a){return r("span",{},[r("a",{on:{click:function(t){return e.goDetails(a.engineerId)}}},[e._v("查看")])])}}])})],1),r("a-drawer",{attrs:{title:"创建工程",width:"enterprise"==e.userOptions.type?1200:700,visible:e.createDrawer,"body-style":{"padding-top":"18px",paddingBottom:"80px"}},on:{"update:visible":function(t){e.createDrawer=t},close:function(){e.createDrawer=!1,e.loading=!1}}},[e.createDrawer?r("create-form",{ref:"createForm",attrs:{options:e.userOptions}}):e._e(),r("div",{style:{position:"absolute",right:0,bottom:0,width:"100%",borderTop:"1px solid #e9e9e9",padding:"10px 16px",background:"#fff",textAlign:"left",zIndex:1}},[r("a-button",{style:{marginRight:"20px"},on:{click:function(){e.createDrawer=!1,e.loading=!1}}},[e._v("取消")]),r("a-button",{attrs:{loading:e.loading,type:"primary"},nativeOn:{click:function(t){return t.preventDefault(),e.submit.apply(null,arguments)}}},[e.loading?r("span",[e._v("添 加 中")]):r("span",[e._v("添 加")])])],1)],1),r("a-drawer",{attrs:{title:"发布详情",width:650,visible:e.detailDrawer,"body-style":{"padding-top":"0px"}},on:{"update:visible":function(t){e.detailDrawer=t},close:function(){e.detailDrawer=!1,e.loading=!1}}},[e.detailDrawer?r("detail-form",{ref:"detailForm",attrs:{engineerId:e.engineerId,options:e.userOptions}}):e._e()],1)],1)}),[],!1,null,null,null);t.default=I.exports},"3d73":function(e,t,r){"use strict";var a=(r("caad"),r("2532"),{name:"RightToolbar",data:function(){return{value:[],title:"显示/隐藏",open:!1,spin:!1}},props:{showSearch:{type:Boolean,default:!0},columns:{type:Array}},created:function(){for(var e in this.columns)!1===this.columns[e].visible&&this.value.push(parseInt(e))},methods:{toggleSearch:function(){this.$emit("update:showSearch",!this.showSearch)},refresh:function(){this.$emit("queryTable")},dataChange:function(e){for(var t in this.columns){var r=this.columns[t].key;this.columns[t].visible=!e.includes(r)}},showColumn:function(){this.open=!0}}}),n=a,i=(r("668e"),r("2877")),s=Object(i.a)(n,(function(){var e=this,t=e.$createElement,r=e._self._c||t;return r("div",{staticClass:"top-right-btn"},[r("a-row",{attrs:{type:"flex",justify:"end"}},[r("a-button-group",[r("a-tooltip",{staticClass:"item",attrs:{title:e.showSearch?"隐藏搜索":"显示搜索",placement:"top"}},[r("a-button",{attrs:{shape:"circle",icon:"search"},on:{click:function(t){return e.toggleSearch()}}})],1),r("a-tooltip",{staticClass:"item",attrs:{title:"刷新",placement:"top"}},[r("a-button",{attrs:{shape:"circle",icon:"sync"},on:{click:function(t){return e.refresh()}}})],1)],1)],1)],1)}),[],!1,null,"84deea80",null),o=s.exports;t.a={components:{RightToolbar:o},data:function(){return{showSearch:!1}},methods:{search:function(){this.pagination.current=1,this.getList()}}}},"408a":function(e,t,r){var a=r("e330");e.exports=a(1..valueOf)},"432b":function(e,t,r){"use strict";r.d(t,"a",(function(){return i}));var a=r("5530"),n=r("2f62"),i={computed:Object(a.a)(Object(a.a)({},Object(n.d)({layout:function(e){return e.app.layout},navTheme:function(e){return e.app.theme},primaryColor:function(e){return e.app.color},colorWeak:function(e){return e.app.weak},fixedHeader:function(e){return e.app.fixedHeader},fixedSidebar:function(e){return e.app.fixedSidebar},contentWidth:function(e){return e.app.contentWidth},autoHideHeader:function(e){return e.app.autoHideHeader},isMobile:function(e){return e.app.isMobile},sideCollapsed:function(e){return e.app.sideCollapsed},multiTab:function(e){return e.app.multiTab}})),{},{isTopMenu:function(){return"topmenu"===this.layout}}),methods:{isSideMenu:function(){return!this.isTopMenu}}}},5899:function(e,t){e.exports="\t\n\v\f\r                　\u2028\u2029\ufeff"},"58a8":function(e,t,r){var a=r("e330"),n=r("1d80"),i=r("577e"),s=r("5899"),o=a("".replace),l="["+s+"]",c=RegExp("^"+l+l+"*"),u=RegExp(l+l+"*$"),d=function(e){return function(t){var r=i(n(t));return 1&e&&(r=o(r,c,"")),2&e&&(r=o(r,u,"")),r}};e.exports={start:d(1),end:d(2),trim:d(3)}},"668e":function(e,t,r){"use strict";r("c885")},7156:function(e,t,r){var a=r("1626"),n=r("861d"),i=r("d2bb");e.exports=function(e,t,r){var s,o;return i&&a(s=t.constructor)&&s!==r&&n(o=s.prototype)&&o!==r.prototype&&i(e,o),e}},a9e3:function(e,t,r){"use strict";var a=r("83ab"),n=r("da84"),i=r("e330"),s=r("94ca"),o=r("6eeb"),l=r("1a2d"),c=r("7156"),u=r("3a9b"),d=r("d9b5"),g=r("c04e"),m=r("d039"),p=r("241c").f,f=r("06cf").f,h=r("9bf2").f,v=r("408a"),b=r("58a8").trim,_="Number",y=n[_],w=y.prototype,I=n.TypeError,k=i("".slice),x=i("".charCodeAt),L=function(e){var t=g(e,"number");return"bigint"==typeof t?t:F(t)},F=function(e){var t,r,a,n,i,s,o,l,c=g(e,"number");if(d(c))throw I("Cannot convert a Symbol value to a number");if("string"==typeof c&&c.length>2)if(c=b(c),43===(t=x(c,0))||45===t){if(88===(r=x(c,2))||120===r)return NaN}else if(48===t){switch(x(c,1)){case 66:case 98:a=2,n=49;break;case 79:case 111:a=8,n=55;break;default:return+c}for(s=(i=k(c,2)).length,o=0;o<s;o++)if((l=x(i,o))<48||l>n)return NaN;return parseInt(i,a)}return+c};if(s(_,!y(" 0o1")||!y("0b1")||y("+0x1"))){for(var S,C=function(e){var t=arguments.length<1?0:y(L(e)),r=this;return u(w,r)&&m((function(){v(r)}))?c(Object(t),r,C):t},N=a?p(y):"MAX_VALUE,MIN_VALUE,NaN,NEGATIVE_INFINITY,POSITIVE_INFINITY,EPSILON,MAX_SAFE_INTEGER,MIN_SAFE_INTEGER,isFinite,isInteger,isNaN,isSafeInteger,parseFloat,parseInt,fromString,range".split(","),O=0;N.length>O;O++)l(y,S=N[O])&&!l(C,S)&&h(C,S,f(y,S));C.prototype=w,w.constructor=C,o(n,_,C)}},c885:function(e,t,r){}}]);