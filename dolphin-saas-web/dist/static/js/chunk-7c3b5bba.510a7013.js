(window.webpackJsonp=window.webpackJsonp||[]).push([["chunk-7c3b5bba"],{"289b":function(t,e,a){"use strict";a.r(e);var s=a("2909"),r=a("5530"),n=(a("d3b7"),a("365c")),i=a("432b"),o=a("3d73"),c={0:{status:"default",text:"待初始化"},1:{status:"success",text:"运行中"},2:{status:"warning",text:"更新中"},3:{status:"error",text:"停用"}},u={mixins:[i.a],props:{assetInfo:{type:Object,default:!0},serverList:{type:Array,default:!0}},data:function(){return{form:this.$form.createForm(this)}},filters:{statusFilter:function(t){return c[t].text},statusTypeFilter:function(t){return c[t].status}},methods:{},created:function(){}},l=u,d=a("2877"),m=Object(d.a)(l,(function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",[a("a-divider",{attrs:{orientation:"left"}},[t._v("基础信息")]),a("a-descriptions",{attrs:{size:"small",column:t.isMobile?1:2,layout:"vertical",bordered:""}},[a("a-descriptions-item",{attrs:{label:"实例ID"}},[t._v(t._s(t.assetInfo.cvmInstanceId||"无"))]),a("a-descriptions-item",{attrs:{label:"别名"}},[t._v(t._s(t.assetInfo.cvmTagName||"无"))]),a("a-descriptions-item",{attrs:{label:"标签"}},[t._l(t.assetInfo.serviceLabel,(function(e){return a("a-tag",{key:e,staticClass:"ml10",attrs:{size:"small",effect:"plain"}},[t._v(t._s(e))])})),t.assetInfo.serviceLabel&&0!=t.assetInfo.serviceLabel.length?t._e():a("span",[t._v("无")])],2),a("a-descriptions-item",{attrs:{label:"所在云/归属地/归属区域"}},[t._v(" "+t._s(t.assetInfo.regionSource||"无")+" / "+t._s(t.assetInfo.cvmRegionId||"无")+" / "+t._s(t.assetInfo.cvmRegionSource||"无")+" ")]),a("a-descriptions-item",{attrs:{label:"状态"}},[t.assetInfo.cvmStatus?a("span",[a("a-badge",{attrs:{status:t._f("statusTypeFilter")(t.assetInfo.cvmStatus),text:t._f("statusFilter")(t.assetInfo.cvmStatus)}})],1):t._e()]),a("a-descriptions-item",{attrs:{label:"内网IP"}},[t._v(t._s(t.assetInfo.cvmClusterInSideIP||"无"))]),a("a-descriptions-item",{attrs:{label:"外网IP"}},[t._v(t._s(t.assetInfo.cvmClusterOutSideIP||"无"))]),a("a-descriptions-item",{attrs:{label:"创建时间"}},[t._v(t._s(t.assetInfo.cvmCreateTime||"无"))]),a("a-descriptions-item",{attrs:{label:"备注"}},[t._v(t._s(t.assetInfo.cvmRemark||"无"))]),a("a-descriptions-item",{attrs:{label:"更新时间"}},[t._v(t._s(t.assetInfo.cvmUpdateTime||"无"))])],1),a("a-divider",{attrs:{orientation:"left"}},[t._v("基本资料")]),a("a-form",{attrs:{form:t.form,"label-col":{span:5},layout:"horizontal","wrapper-col":{span:17}}},[a("a-form-item",{attrs:{label:"绑定标签"}},[a("a-select",{directives:[{name:"decorator",rawName:"v-decorator",value:["tagId",{rules:[{required:!0,message:"云资产标签不能为空!"}]}],expression:"[\n        'tagId',\n        { rules: [{ required: true, message: '云资产标签不能为空!' }] },\n      ]"}],attrs:{placeholder:"请选择标签"}},t._l(t.serverList,(function(e){return a("a-select-option",{key:e.value},[t._v(t._s(e.label))])})),1)],1),a("a-form-item",{attrs:{label:"用户名"}},[a("a-input",{directives:[{name:"decorator",rawName:"v-decorator",value:["serviceUsername",{rules:[{required:!0,message:"用户名不能为空!"}]}],expression:"['serviceUsername', { rules: [{ required: true, message: '用户名不能为空!' }] }]"}],attrs:{placeholder:"请输入用户名"}})],1),a("a-form-item",{attrs:{label:"端口"}},[a("a-input",{directives:[{name:"decorator",rawName:"v-decorator",value:["servicePort",{rules:[{required:!0,message:"端口不能为空!"}]}],expression:"['servicePort', { rules: [{ required: true, message: '端口不能为空!' }] }]"}],attrs:{placeholder:"请输入端口"}})],1),a("a-form-item",{attrs:{label:"密码"}},[a("a-input-password",{directives:[{name:"decorator",rawName:"v-decorator",value:["servicePassword",{rules:[{required:!0,message:"密码不能为空!"}]}],expression:"['servicePassword', { rules: [{ required: true, message: '密码不能为空!' }] }]"}],attrs:{placeholder:"请输入密码"}})],1)],1)],1)}),[],!1,null,null,null),f=m.exports,p=[{title:"ID/名称",className:"mw100",scopedSlots:{customRender:"cvmID"}},{title:"状态",dataIndex:"cvmStatus",className:"mw80",scopedSlots:{customRender:"status"}},{title:"主IPV4地址",className:"mw120",scopedSlots:{customRender:"IP"}},{title:"预计到期时间",dataIndex:"cvmCost",className:"mw100",scopedSlots:{customRender:"cvmCost"}},{title:"内存/硬盘/cpu",className:"mw120",dataIndex:"cvmConfig"},{title:"归属",className:"mw70",scopedSlots:{customRender:"region"}},{title:"标签",className:"mw70",dataIndex:"serviceLabel",scopedSlots:{customRender:"serviceLabel"}},{title:"更新时间",className:"mw100",dataIndex:"cvmUpdateTime",scopedSlots:{customRender:"updateTime"}},{title:"操作",dataIndex:"action",align:"center",className:"mw80",scopedSlots:{customRender:"action"}}],v={0:{status:"default",text:"待初始化"},1:{status:"success",text:"运行中"},2:{status:"warning",text:"更新中"},3:{status:"error",text:"停用"}},h={components:{AssetForm:f},mixins:[i.a,o.a],data:function(){return{searchForm:{IPv4:void 0,cvmRegionSource:void 0,tagId:void 0,cvmTagName:void 0},pagination:{current:1,pageSize:10,total:0,showQuickJumper:!0,hideOnSinglePage:!0,showTotal:function(t){return"共 ".concat(t," 条数据")}},list:[],columns:p,loading:!1,listLoading:!1,updateLoading:!1,assetInfo:{cid:"",cvmClusterOutSideIP:"",cvmTag:[],cvmCvmCost:"",cvmRemark:"",cvmassetConfig:"",serviceLists:{},cvmInstanceId:"",cvmRegionId:"",cvmRegionSource:"",cvmClusterInSideIP:"",cvmUpdateTime:"",cvmDelete:0,cvmStatus:0,cvmTagName:"",cvmCreateTime:""},cloudList:[],serverList:[],drawerForm:!1}},filters:{statusFilter:function(t){return v[t].text},statusTypeFilter:function(t){return v[t].status}},methods:{search:function(){this.pagination.current=1,this.getList()},tableChange:function(t){this.pagination=t,this.getList()},getList:function(){var t=this;this.listLoading=!0;var e=this.pagination,a=e.current,s=e.pageSize;Object(n.a)(Object(r.a)(Object(r.a)({},{page:a,pageSize:s}),this.searchForm)).then((function(e){var a=e.status,s=e.data,r=e.msg;if(1===a){var n=s.page,i=s.pageSize,o=s.total,c=s.list;t.list=c||[],t.pagination={current:n,pageSize:i,total:o}}else t.$message.error(r);t.listLoading=!1})).catch((function(e){t.listLoading=!1}))},judgmentAsset:function(){var t=this,e=this;this.$destroyAll(),Object(n.W)().then((function(a){var s=a.status;a.msg,1!=s&&t.$confirm({title:"提示",content:"当前暂无资产",okText:"去加载",maskClosable:!0,onOk:function(){return e.updateAsset(),new Promise((function(t,e){t()})).catch((function(){}))},onCancel:function(){}})})).catch((function(t){}))},checkAsset:function(){var t=this;Object(n.c)().then((function(e){var a=e.status;e.data,e.msg,t.updateLoading=1!==a})).catch((function(t){}))},updateAsset:function(){var t=this;this.updateLoading=!0,Object(n.nb)().then((function(e){var a=e.status,s=e.msg;1===a?(t.pagination.current=1,t.getList(),t.checkAsset(),t.$message.success(s)):t.$message.error(s),t.updateLoading=!1})).catch((function(e){t.updateLoading=!1}))},goDetails:function(t){var e=this,a=t.cid;this.drawerForm=!0,Object(n.r)({cid:a}).then((function(t){var a=t.status,s=t.data,n=t.msg;1===a?e.assetInfo=Object(r.a)({},s):e.$message.error(n)})).catch((function(t){})),Object(n.R)().then((function(t){var a=t.status,r=t.data,n=t.msg;1===a?e.serverList=Object(s.a)(r):e.$message.error(n)})).catch((function(t){}))},submit:function(){var t=this;this.loading=!0;(0,this.$refs.assetForm.form.validateFields)(["tagId","servicePort","serviceUsername","servicePassword"],{force:!0},(function(e,a){if(e)return t.loading=!1,!1;Object(n.b)(Object(r.a)(Object(r.a)({},a),{cid:t.assetInfo.cid})).then((function(e){var a=e.status,s=(e.data,e.msg);1===a?(t.$message.success(s),t.pagination.current=1,t.getList(),t.drawerForm=!1):t.$message.error(s),t.loading=!1})).catch((function(){t.loading=!1}))}))},getCloudList:function(){var t=this;Object(n.t)().then((function(e){var a=e.status,s=e.data,r=e.msg;1===a?t.cloudList=s||[]:t.$message.error(r)})).catch((function(){})),Object(n.R)().then((function(e){var a=e.status,r=e.data,n=e.msg;1===a?t.serverList=Object(s.a)(r):t.$message.error(n)})).catch((function(t){}))}},created:function(){this.judgmentAsset(),this.checkAsset(),this.getCloudList(),this.getList()},mounted:function(){},beforeCreate:function(){},beforeMount:function(){},beforeUpdate:function(){},updated:function(){},beforeDestroy:function(){},destroyed:function(){},activated:function(){}},g=h,b=Object(d.a)(g,(function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",[a("a-card",{attrs:{bordered:!1}},[a("a-form",{directives:[{name:"show",rawName:"v-show",value:t.showSearch,expression:"showSearch"}],staticClass:"mb20",attrs:{layout:"inline"},model:{value:t.searchForm,callback:function(e){t.searchForm=e},expression:"searchForm"}},[a("a-form-item",{attrs:{label:"IP地址"}},[a("a-input",{attrs:{placeholder:"请输入IP地址"},nativeOn:{keyup:function(e){return!e.type.indexOf("key")&&t._k(e.keyCode,"enter",13,e.key,"Enter")?null:t.search.apply(null,arguments)}},model:{value:t.searchForm.assetIp,callback:function(e){t.$set(t.searchForm,"assetIp",e)},expression:"searchForm.assetIp"}})],1),a("a-form-item",{attrs:{label:"归属云"}},[a("a-select",{staticClass:"mw180",attrs:{placeholder:"请选择归属云"},model:{value:t.searchForm.cloudId,callback:function(e){t.$set(t.searchForm,"cloudId",e)},expression:"searchForm.cloudId"}},t._l(t.cloudList,(function(e){return a("a-select-option",{key:e.value,attrs:{value:e.value}},[t._v(t._s(e.label))])})),1)],1),a("a-form-item",{attrs:{label:"服务标签"}},[a("a-select",{staticClass:"mw180",attrs:{placeholder:"请选择服务标签"},model:{value:t.searchForm.tagId,callback:function(e){t.$set(t.searchForm,"tagId",e)},expression:"searchForm.tagId"}},t._l(t.serverList,(function(e){return a("a-select-option",{key:e.value,attrs:{value:e.value}},[t._v(t._s(e.label))])})),1)],1),a("a-form-item",{attrs:{label:"实例名称"}},[a("a-input",{attrs:{placeholder:"请输入实例名称"},nativeOn:{keyup:function(e){return!e.type.indexOf("key")&&t._k(e.keyCode,"enter",13,e.key,"Enter")?null:t.search.apply(null,arguments)}},model:{value:t.searchForm.cvmTagName,callback:function(e){t.$set(t.searchForm,"cvmTagName",e)},expression:"searchForm.cvmTagName"}})],1),a("a-form-item",[a("a-button",{attrs:{type:"primary"},nativeOn:{click:function(e){return e.preventDefault(),t.search.apply(null,arguments)}}},[t._v("查询")]),a("a-button",{staticStyle:{"margin-left":"8px"},on:{click:function(){return t.searchForm={}}}},[t._v("重置")])],1)],1),a("a-row",{attrs:{gutter:10,type:"flex",justify:"space-between"}},[a("a-col",{attrs:{span:12}},[a("a-button",{attrs:{type:"primary",icon:"sync",ghost:"",loading:t.updateLoading},on:{click:t.updateAsset}},[t.updateLoading?a("span",[t._v("加载云资产数据中...")]):a("span",[t._v("加载云资产数据")])])],1),a("a-col",{attrs:{span:2}},[a("right-toolbar",{attrs:{showSearch:t.showSearch},on:{"update:showSearch":function(e){t.showSearch=e},"update:show-search":function(e){t.showSearch=e},queryTable:t.getList}})],1)],1),a("div",{staticClass:"table-operator"}),a("a-table",{ref:"table",attrs:{size:"middle",columns:t.columns,dataSource:t.list,loading:t.listLoading,pagination:t.pagination,rowKey:function(t,e){return e}},on:{change:t.tableChange},scopedSlots:t._u([{key:"cvmID",fn:function(e,s){return a("span",{},[t._v(" "+t._s(s.cvmInstanceId)+" "),a("br"),t._v(" "+t._s(s.cvmTagName)+" ")])}},{key:"IP",fn:function(e,s){return a("span",{},[a("span",{staticClass:"text-success"},[s.cvmClusterOutSideIP?a("span",[t._v(t._s(s.cvmClusterOutSideIP)+"(公)")]):t._e(),a("br"),s.cvmClusterInSideIP?a("span",[t._v(t._s(s.cvmClusterInSideIP)+"(内)")]):t._e()])])}},{key:"cvmCost",fn:function(e){return a("span",{},[t._v(t._s(e)+"天")])}},{key:"region",fn:function(e,s){return a("span",{},[t._v(" "+t._s(s.regionSource)+" "),a("br"),t._v(" "+t._s(s.cvmRegionId)+" ")])}},{key:"serviceLabel",fn:function(e){return a("span",{},[t._l(e,(function(e){return a("a-tag",{key:e,staticClass:"ml5 mb5",attrs:{size:"small",effect:"plain"}},[t._v(t._s(e))])})),0==e.length?a("span",[t._v("无服务")]):t._e()],2)}},{key:"status",fn:function(e){return a("span",{},[a("a-badge",{attrs:{status:t._f("statusTypeFilter")(e),text:t._f("statusFilter")(e)}})],1)}},{key:"updateTime",fn:function(e){return a("span",{},[a("span",[t._v(t._s(e||"无"))])])}},{key:"action",fn:function(e,s){return a("span",{},[[a("a",{on:{click:function(e){return t.goDetails(s)}}},[t._v("查看")])]],2)}}])})],1),a("a-drawer",{attrs:{title:"云资产详情",width:t.isMobile?300:600,visible:t.drawerForm,"body-style":{"padding-top":"0px",paddingBottom:"80px"}},on:{"update:visible":function(e){t.drawerForm=e},close:function(){t.drawerForm=!1,t.loading=!1}}},[a("asset-form",{key:t.drawerForm,ref:"assetForm",attrs:{assetInfo:t.assetInfo,serverList:t.serverList}}),a("div",{style:{position:"absolute",right:0,bottom:0,width:"100%",borderTop:"1px solid #e9e9e9",padding:"10px 16px",background:"#fff",textAlign:"left",zIndex:1}},[a("a-button",{style:{marginRight:"20px"},on:{click:function(){t.drawerForm=!1,t.loading=!1}}},[t._v("取消")]),a("a-button",{attrs:{loading:t.loading,type:"primary"},nativeOn:{click:function(e){return e.preventDefault(),t.submit.apply(null,arguments)}}},[t.loading?a("span",[t._v("保 存 中")]):a("span",[t._v("保 存")])])],1)],1)],1)}),[],!1,null,null,null);e.default=b.exports},2909:function(t,e,a){"use strict";function s(t,e){(null==e||e>t.length)&&(e=t.length);for(var a=0,s=new Array(e);a<e;a++)s[a]=t[a];return s}function r(t){return function(t){if(Array.isArray(t))return s(t)}(t)||function(t){if("undefined"!=typeof Symbol&&null!=t[Symbol.iterator]||null!=t["@@iterator"])return Array.from(t)}(t)||function(t,e){if(t){if("string"==typeof t)return s(t,e);var a=Object.prototype.toString.call(t).slice(8,-1);return"Object"===a&&t.constructor&&(a=t.constructor.name),"Map"===a||"Set"===a?Array.from(t):"Arguments"===a||/^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(a)?s(t,e):void 0}}(t)||function(){throw new TypeError("Invalid attempt to spread non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method.")}()}a.d(e,"a",(function(){return r})),a("a4d3"),a("e01a"),a("d3b7"),a("d28b"),a("3ca3"),a("ddb0"),a("a630"),a("fb6a"),a("b0c0"),a("ac1f"),a("00b4")},"3d73":function(t,e,a){"use strict";var s=(a("caad"),a("2532"),{name:"RightToolbar",data:function(){return{value:[],title:"显示/隐藏",open:!1,spin:!1}},props:{showSearch:{type:Boolean,default:!0},columns:{type:Array}},created:function(){for(var t in this.columns)!1===this.columns[t].visible&&this.value.push(parseInt(t))},methods:{toggleSearch:function(){this.$emit("update:showSearch",!this.showSearch)},refresh:function(){this.$emit("queryTable")},dataChange:function(t){for(var e in this.columns){var a=this.columns[e].key;this.columns[e].visible=!t.includes(a)}},showColumn:function(){this.open=!0}}}),r=s,n=(a("668e"),a("2877")),i=Object(n.a)(r,(function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",{staticClass:"top-right-btn"},[a("a-row",{attrs:{type:"flex",justify:"end"}},[a("a-button-group",[a("a-tooltip",{staticClass:"item",attrs:{title:t.showSearch?"隐藏搜索":"显示搜索",placement:"top"}},[a("a-button",{attrs:{shape:"circle",icon:"search"},on:{click:function(e){return t.toggleSearch()}}})],1),a("a-tooltip",{staticClass:"item",attrs:{title:"刷新",placement:"top"}},[a("a-button",{attrs:{shape:"circle",icon:"sync"},on:{click:function(e){return t.refresh()}}})],1)],1)],1)],1)}),[],!1,null,"84deea80",null),o=i.exports;e.a={components:{RightToolbar:o},data:function(){return{showSearch:!1}},methods:{search:function(){this.pagination.current=1,this.getList()}}}},"432b":function(t,e,a){"use strict";a.d(e,"a",(function(){return n}));var s=a("5530"),r=a("2f62"),n={computed:Object(s.a)(Object(s.a)({},Object(r.d)({layout:function(t){return t.app.layout},navTheme:function(t){return t.app.theme},primaryColor:function(t){return t.app.color},colorWeak:function(t){return t.app.weak},fixedHeader:function(t){return t.app.fixedHeader},fixedSidebar:function(t){return t.app.fixedSidebar},contentWidth:function(t){return t.app.contentWidth},autoHideHeader:function(t){return t.app.autoHideHeader},isMobile:function(t){return t.app.isMobile},sideCollapsed:function(t){return t.app.sideCollapsed},multiTab:function(t){return t.app.multiTab}})),{},{isTopMenu:function(){return"topmenu"===this.layout}}),methods:{isSideMenu:function(){return!this.isTopMenu}}}},"4df4":function(t,e,a){"use strict";var s=a("da84"),r=a("0366"),n=a("c65b"),i=a("7b0b"),o=a("9bdd"),c=a("e95a"),u=a("68ee"),l=a("07fa"),d=a("8418"),m=a("9a1f"),f=a("35a1"),p=s.Array;t.exports=function(t){var e=i(t),a=u(this),s=arguments.length,v=s>1?arguments[1]:void 0,h=void 0!==v;h&&(v=r(v,s>2?arguments[2]:void 0));var g,b,y,I,_,w,S=f(e),x=0;if(!S||this==p&&c(S))for(g=l(e),b=a?new this(g):p(g);g>x;x++)w=h?v(e[x],x):e[x],d(b,x,w);else for(_=(I=m(e,S)).next,b=a?new this:[];!(y=n(_,I)).done;x++)w=h?o(I,v,[y.value,x],!0):y.value,d(b,x,w);return b.length=x,b}},"668e":function(t,e,a){"use strict";a("c885")},"9bdd":function(t,e,a){var s=a("825a"),r=a("2a62");t.exports=function(t,e,a,n){try{return n?e(s(a)[0],a[1]):e(a)}catch(e){r(t,"throw",e)}}},a630:function(t,e,a){var s=a("23e7"),r=a("4df4"),n=!a("1c7e")((function(t){Array.from(t)}));s({target:"Array",stat:!0,forced:n},{from:r})},c885:function(t,e,a){},d28b:function(t,e,a){a("746f")("iterator")},e01a:function(t,e,a){"use strict";var s=a("23e7"),r=a("83ab"),n=a("da84"),i=a("e330"),o=a("1a2d"),c=a("1626"),u=a("3a9b"),l=a("577e"),d=a("9bf2").f,m=a("e893"),f=n.Symbol,p=f&&f.prototype;if(r&&c(f)&&(!("description"in p)||void 0!==f().description)){var v={},h=function(){var t=arguments.length<1||void 0===arguments[0]?void 0:l(arguments[0]),e=u(p,this)?new f(t):void 0===t?f():f(t);return""===t&&(v[e]=!0),e};m(h,f),h.prototype=p,p.constructor=h;var g="Symbol(test)"==String(f("test")),b=i(p.toString),y=i(p.valueOf),I=/^Symbol\((.*)\)[^)]+$/,_=i("".replace),w=i("".slice);d(p,"description",{configurable:!0,get:function(){var t=y(this),e=b(t);if(o(v,t))return"";var a=g?w(e,7,-1):_(e,I,"$1");return""===a?void 0:a}}),s({global:!0,forced:!0},{Symbol:h})}},fb6a:function(t,e,a){"use strict";var s=a("23e7"),r=a("da84"),n=a("e8b5"),i=a("68ee"),o=a("861d"),c=a("23cb"),u=a("07fa"),l=a("fc6a"),d=a("8418"),m=a("b622"),f=a("1dde"),p=a("f36a"),v=f("slice"),h=m("species"),g=r.Array,b=Math.max;s({target:"Array",proto:!0,forced:!v},{slice:function(t,e){var a,s,r,m=l(this),f=u(m),v=c(t,f),y=c(void 0===e?f:e,f);if(n(m)&&(a=m.constructor,i(a)&&(a===g||n(a.prototype))?a=void 0:o(a)&&(null===(a=a[h])&&(a=void 0)),a===g||void 0===a))return p(m,v,y);for(s=new(void 0===a?g:a)(b(y-v,0)),r=0;v<y;v++,r++)v in m&&d(s,r,m[v]);return s.length=r,s}})}}]);