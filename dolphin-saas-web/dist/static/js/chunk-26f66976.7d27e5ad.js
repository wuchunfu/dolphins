(window.webpackJsonp=window.webpackJsonp||[]).push([["chunk-26f66976"],{1276:function(t,e,n){"use strict";var a=n("2ba4"),r=n("c65b"),i=n("e330"),s=n("d784"),o=n("44e7"),c=n("825a"),u=n("1d80"),l=n("4840"),d=n("8aa5"),f=n("50c4"),p=n("577e"),g=n("dc4a"),m=n("f36a"),h=n("14c3"),v=n("9263"),b=n("9f7f"),y=n("d039"),x=b.UNSUPPORTED_Y,w=4294967295,C=Math.min,S=[].push,_=i(/./.exec),k=i(S),N=i("".slice),I=!y((function(){var t=/(?:)/,e=t.exec;t.exec=function(){return e.apply(this,arguments)};var n="ab".split(t);return 2!==n.length||"a"!==n[0]||"b"!==n[1]}));s("split",(function(t,e,n){var i;return i="c"=="abbc".split(/(b)*/)[1]||4!="test".split(/(?:)/,-1).length||2!="ab".split(/(?:ab)*/).length||4!=".".split(/(.?)(.?)/).length||".".split(/()()/).length>1||"".split(/.?/).length?function(t,n){var i=p(u(this)),s=void 0===n?w:n>>>0;if(0===s)return[];if(void 0===t)return[i];if(!o(t))return r(e,i,t,s);for(var c,l,d,f=[],g=(t.ignoreCase?"i":"")+(t.multiline?"m":"")+(t.unicode?"u":"")+(t.sticky?"y":""),h=0,b=new RegExp(t.source,g+"g");(c=r(v,b,i))&&!((l=b.lastIndex)>h&&(k(f,N(i,h,c.index)),c.length>1&&c.index<i.length&&a(S,f,m(c,1)),d=c[0].length,h=l,f.length>=s));)b.lastIndex===c.index&&b.lastIndex++;return h===i.length?!d&&_(b,"")||k(f,""):k(f,N(i,h)),f.length>s?m(f,0,s):f}:"0".split(void 0,0).length?function(t,n){return void 0===t&&0===n?[]:r(e,this,t,n)}:e,[function(e,n){var a=u(this),s=null==e?void 0:g(e,t);return s?r(s,e,a,n):r(i,p(a),e,n)},function(t,a){var r=c(this),s=p(t),o=n(i,r,s,a,i!==e);if(o.done)return o.value;var u=l(r,RegExp),g=r.unicode,m=(r.ignoreCase?"i":"")+(r.multiline?"m":"")+(r.unicode?"u":"")+(x?"g":"y"),v=new u(x?"^(?:"+r.source+")":r,m),b=void 0===a?w:a>>>0;if(0===b)return[];if(0===s.length)return null===h(v,s)?[s]:[];for(var y=0,S=0,_=[];S<s.length;){v.lastIndex=x?0:S;var I,A=h(v,x?N(s,S):s);if(null===A||(I=C(f(v.lastIndex+(x?S:0)),s.length))===y)S=d(s,S,g);else{if(k(_,N(s,y,S)),_.length===b)return _;for(var F=1;F<=A.length-1;F++)if(k(_,A[F]),_.length===b)return _;S=y=I}}return k(_,N(s,y)),_}]}),!I,x)},"408a":function(t,e,n){var a=n("e330");t.exports=a(1..valueOf)},"498a":function(t,e,n){"use strict";var a=n("23e7"),r=n("58a8").trim;a({target:"String",proto:!0,forced:n("c8d2")("trim")},{trim:function(){return r(this)}})},5539:function(t,e,n){"use strict";n.r(e);var a=n("ade3"),r=n("5530"),i=(n("d3b7"),n("a9e3"),n("a15b"),n("d81d"),n("f933"));n("4de4"),n("498a"),n("ac1f"),n("1276");var s=function(){var t=arguments.length>0&&void 0!==arguments[0]?arguments[0]:"",e=arguments.length>1?arguments[1]:void 0,n=0;return t.split("").reduce((function(t,a){var r=a.charCodeAt(0);return(n+=r>=0&&r<=128?1:2)<=e?t+a:t}),"")},o={name:"Ellipsis",components:{Tooltip:i.a},props:{prefixCls:{type:String,default:"ant-pro-ellipsis"},tooltip:{type:Boolean},length:{type:Number,required:!0},lines:{type:Number,default:1},fullWidthRecognition:{type:Boolean,default:!1}},methods:{getStrDom:function(t,e){return(0,this.$createElement)("span",[s(t,this.length)+(e>this.length?"...":"")])},getTooltip:function(t,e){var n=this.$createElement;return n(i.a,[n("template",{slot:"title"},[t]),this.getStrDom(t,e)])}},render:function(){var t=this.$props,e=t.tooltip,n=t.length,a=this.$slots.default.map((function(t){return t.text})).join(""),r=function(){return(arguments.length>0&&void 0!==arguments[0]?arguments[0]:"").split("").reduce((function(t,e){var n=e.charCodeAt(0);return n>=0&&n<=128?t+1:t+2}),0)}(a);return e&&r>n?this.getTooltip(a,r):this.getStrDom(a,r)}},c=o,u=n("2877"),l=Object(u.a)(c,undefined,undefined,!1,null,null,null).exports,d=n("365c"),f={props:{isCreate:{type:Boolean,default:!0},cloudList:{type:Array,default:!0}},data:function(){return{form:this.$form.createForm(this)}},methods:{},created:function(){}},p=Object(u.a)(f,(function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("a-form",{attrs:{form:t.form,"label-col":{span:5},layout:"horizontal","wrapper-col":{span:17}}},[n("a-form-item",{attrs:{label:"AccessKey"}},[n("a-input",{directives:[{name:"decorator",rawName:"v-decorator",value:["accessKey",{rules:[{required:!0,message:"AccessKey不能为空!"}]}],expression:"['accessKey', { rules: [{ required: true, message: 'AccessKey不能为空!' }] }]"}],attrs:{placeholder:"请输入AccessKey"}})],1),n("a-form-item",{attrs:{label:"AccessSecret"}},[n("a-input",{directives:[{name:"decorator",rawName:"v-decorator",value:["accessSecret",{rules:[{required:!0,message:"AccessSecret不能为空!"}]}],expression:"['accessSecret', { rules: [{ required: true, message: 'AccessSecret不能为空!' }] }]"}],attrs:{placeholder:"请输入AccessSecret"}})],1),n("a-form-item",{attrs:{label:"云厂商",hasFeedback:t.isCreate}},[n("a-select",{directives:[{name:"decorator",rawName:"v-decorator",value:["typeName",{rules:[t.isCreate?{required:!0,message:"请选择云厂商!"}:{}]}],expression:"[\n        'typeName',\n        { rules: [isCreate?{ required: true, message: '请选择云厂商!' }:{}] },\n      ]"}],attrs:{placeholder:"请选择云厂商",disabled:!t.isCreate}},t._l(t.cloudList,(function(e){return n("a-select-option",{key:t.isCreate?e.value:e.label},[t._v(t._s(e.label))])})),1)],1)],1)}),[],!1,null,null,null),g=p.exports,m=[{title:"云厂商名称",className:"mw100",dataIndex:"TypeName"},{title:"accessKey",dataIndex:"AccessKey",scopedSlots:{customRender:"AccessKey"}},{title:"accessSecret",dataIndex:"AccessSecret",scopedSlots:{customRender:"AccessSecret"}},{title:"创建时间",className:"mw100",dataIndex:"CreateTime"},{title:"更新时间",className:"mw100",dataIndex:"UpdateTime",scopedSlots:{customRender:"updateTime"}},{key:"Vdefaults",slots:{title:"defaults"},dataIndex:"Vdefaults",scopedSlots:{customRender:"vdefaults"}},{title:"状态",className:"mw90",dataIndex:"Status",scopedSlots:{customRender:"status"}},{title:"操作",dataIndex:"action",align:"center",width:"150px",scopedSlots:{customRender:"action"}}],h={0:{status:"default",text:"待检测"},1:{status:"success",text:"启用"},2:{status:"error",text:"无效"}},v={components:{Ellipsis:l,ConfigForm:g},data:function(){return{searchForm:{instanceid:"",aliasname:""},pagination:{current:1,pageSize:10,total:0,showQuickJumper:!0,hideOnSinglePage:!0,showTotal:function(t){return"共 ".concat(t," 条数据")}},list:[],columns:m,listLoading:!1,loading:!1,isCreate:!0,drawerForm:!1,cloudList:[],view:"",editVid:""}},filters:{statusFilter:function(t){return h[t].text},statusTypeFilter:function(t){return h[t].status}},computed:{},methods:{search:function(){this.pagination.current=1,this.getList()},tableChange:function(t){this.pagination=t,this.getList()},getList:function(){var t=this;this.listLoading=!0;var e=this.pagination,n=e.current,a=e.pageSize;Object(d.w)(Object(r.a)(Object(r.a)({},{page:n,pageSize:a}),this.searchForm)).then((function(e){var n=e.status,a=e.data,r=e.msg;if(1===n){var i=a.page,s=a.pageSize,o=a.total,c=a.list;t.list=c||[],t.pagination={current:i,pageSize:s,total:o}}else t.$message.error(r);t.listLoading=!1})).catch((function(e){t.listLoading=!1}))},judgmentCloud:function(){var t=this,e=this;this.$destroyAll(),Object(d.X)().then((function(n){var a=n.status;n.msg,1!=a&&t.$confirm({title:"提示",content:"当前暂未绑定云厂商",okText:"去绑定",maskClosable:!0,onOk:function(){return e.create(),new Promise((function(t,e){t()})).catch((function(){}))},onCancel:function(){}})})).catch((function(t){}))},create:function(){this.isCreate=!0,this.drawerForm=!0,this.getCloudList()},setupDefault:function(t,e){var n=this;this.$confirm(Object(a.a)({title:"提示",content:"是否设置此密钥为该厂商的默认密钥？",okText:"设置",onCancel:"取消",maskClosable:!0,onOk:function(){return Object(d.ob)({vid:t,status:e,Vdefaults:1}).then((function(t){var e=t.status,a=(t.data,t.msg);1===e?(n.$message.success(a),n.pagination.current=1,n.getList()):n.$message.error(a)})).catch((function(){})),new Promise((function(t,e){t()})).catch((function(){}))}},"onCancel",(function(){})))},goDetails:function(t){var e=t.vid,n=t.AccessKey,a=t.AccessSecret,r=t.TypeName,i=t.Status;this.isCreate=!1,this.drawerForm=!0,this.editVid=e,this.getCloudList(),this.$nextTick((function(){this.$refs.configForm.form.setFieldsValue({accessKey:n,accessSecret:a,status:i,typeName:r})}))},getCloudList:function(){var t=this;Object(d.t)().then((function(e){var n=e.status,a=e.data,r=e.msg;1===n?t.cloudList=a||[]:t.$message.error(r)})).catch((function(){}))},goView:function(t){this.view==t?this.view="":this.view=t},submit:function(){var t=this;this.loading=!0,(0,this.$refs.configForm.form.validateFields)(this.isCreate?["accessKey","accessSecret","typeName"]:["accessKey","accessSecret"],{force:!0},(function(e,n){if(e)return t.loading=!1,!1;(t.isCreate?Object(d.e)(Object(r.a)({},n)):Object(d.ob)(Object(r.a)(Object(r.a)({},n),{vid:t.editVid}))).then((function(e){var n=e.status,a=(e.data,e.msg);1===n?(t.$message.success(a),t.pagination.current=1,t.getList(),t.drawerForm=!1):t.$message.error(a),t.loading=!1})).catch((function(){t.loading=!1}))}))},delCloud:function(t){var e=this;this.$confirm({title:"提示",content:"此删除操作不可逆，是否删除此云厂商？",okText:"删除",maskClosable:!0,onOk:function(){return Object(d.m)({vid:t}).then((function(t){var n=t.status,a=(t.data,t.msg);1===n?(e.getList(),e.$message.success(a)):e.$message.error(a)})).catch((function(){})),new Promise((function(t,e){t()})).catch((function(){}))},onCancel:function(){}})}},created:function(){this.getList(),this.judgmentCloud()},mounted:function(){},beforeCreate:function(){},beforeMount:function(){},beforeUpdate:function(){},updated:function(){},beforeDestroy:function(){},destroyed:function(){},activated:function(){}},b=v,y=Object(u.a)(b,(function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",[n("a-card",{attrs:{bordered:!1}},[n("div",{staticClass:"table-page-search-wrapper"}),n("div",{staticClass:"table-operator"},[n("a-button",{attrs:{type:"primary",icon:"plus",ghost:""},on:{click:t.create}},[t._v("绑定云厂商")])],1),n("a-table",{ref:"table",attrs:{size:"middle",columns:t.columns,dataSource:t.list,pagination:t.pagination,loading:t.listLoading,rowKey:function(t,e){return e}},on:{change:t.tableChange},scopedSlots:t._u([{key:"AccessKey",fn:function(e,a){return n("span",{},[a.vid==t.view?n("ellipsis",{attrs:{length:10,tooltip:""}},[t._v(t._s(e))]):n("span",[t._v("**********")])],1)}},{key:"AccessSecret",fn:function(e,a){return n("span",{},[a.vid==t.view?n("ellipsis",{attrs:{length:10,tooltip:""}},[t._v(t._s(e))]):n("span",[t._v("**********")])],1)}},{key:"vdefaults",fn:function(e,a){return n("span",{},[1==+e?n("a-tag",{attrs:{color:"#108ee9"}},[t._v("默认密钥")]):n("a-tag",{on:{click:function(e){return t.setupDefault(a.vid,a.Status)}}},[t._v("设为默认")])],1)}},{key:"status",fn:function(e){return n("span",{},[n("a-badge",{attrs:{status:t._f("statusTypeFilter")(e),text:t._f("statusFilter")(e)}})],1)}},{key:"updateTime",fn:function(e){return n("span",{},[n("span",[t._v(t._s(e||"无"))])])}},{key:"action",fn:function(e,a){return n("span",{},[[n("a",{on:{click:function(e){return t.goView(a.vid)}}},[t._v("查看")]),n("a-divider",{attrs:{type:"vertical"}}),n("a",{on:{click:function(e){return t.goDetails(a)}}},[t._v("配置")]),n("a-divider",{attrs:{type:"vertical"}}),n("a",{on:{click:function(e){return t.delCloud(a.vid)}}},[t._v("删除")])]],2)}}])},[n("span",{attrs:{slot:"defaults"},slot:"defaults"},[t._v(" 默认状态 "),n("a-popover",{attrs:{placement:"top"}},[n("template",{slot:"content"},[n("p",[t._v(" 第一个有效秘钥会设定为 "),n("span",{staticClass:"text-blue"},[t._v("默认秘钥")]),t._v("，用于「创建集群」 ")])]),n("template",{slot:"title"},[n("span",[n("span",{staticClass:"text-blue"},[t._v("默认秘钥")]),t._v("说明 ")])]),n("a-icon",{attrs:{type:"info-circle",theme:"twoTone"}})],2)],1)])],1),n("a-drawer",{attrs:{title:t.isCreate?"创建配置":"更新配置",width:600,visible:t.drawerForm,"body-style":{paddingBottom:"80px"}},on:{"update:visible":function(e){t.drawerForm=e},close:function(){t.drawerForm=!1,t.loading=!1}}},[n("config-form",{key:t.drawerForm,ref:"configForm",attrs:{isCreate:t.isCreate,cloudList:t.cloudList}}),n("div",{style:{position:"absolute",right:0,bottom:0,width:"100%",borderTop:"1px solid #e9e9e9",padding:"10px 16px",background:"#fff",textAlign:"left",zIndex:1}},[n("a-button",{style:{marginRight:"20px"},on:{click:function(){t.drawerForm=!1,t.loading=!1}}},[t._v("取消")]),n("a-button",{attrs:{loading:t.loading,type:"primary"},nativeOn:{click:function(e){return e.preventDefault(),t.submit.apply(null,arguments)}}},[t.loading?n("span",[t._v("保 存 中")]):n("span",[t._v("保 存")])])],1)],1)],1)}),[],!1,null,null,null);e.default=y.exports},5899:function(t,e){t.exports="\t\n\v\f\r                　\u2028\u2029\ufeff"},"58a8":function(t,e,n){var a=n("e330"),r=n("1d80"),i=n("577e"),s=n("5899"),o=a("".replace),c="["+s+"]",u=RegExp("^"+c+c+"*"),l=RegExp(c+c+"*$"),d=function(t){return function(e){var n=i(r(e));return 1&t&&(n=o(n,u,"")),2&t&&(n=o(n,l,"")),n}};t.exports={start:d(1),end:d(2),trim:d(3)}},7156:function(t,e,n){var a=n("1626"),r=n("861d"),i=n("d2bb");t.exports=function(t,e,n){var s,o;return i&&a(s=e.constructor)&&s!==n&&r(o=s.prototype)&&o!==n.prototype&&i(t,o),t}},a15b:function(t,e,n){"use strict";var a=n("23e7"),r=n("e330"),i=n("44ad"),s=n("fc6a"),o=n("a640"),c=r([].join),u=i!=Object,l=o("join",",");a({target:"Array",proto:!0,forced:u||!l},{join:function(t){return c(s(this),void 0===t?",":t)}})},a9e3:function(t,e,n){"use strict";var a=n("83ab"),r=n("da84"),i=n("e330"),s=n("94ca"),o=n("6eeb"),c=n("1a2d"),u=n("7156"),l=n("3a9b"),d=n("d9b5"),f=n("c04e"),p=n("d039"),g=n("241c").f,m=n("06cf").f,h=n("9bf2").f,v=n("408a"),b=n("58a8").trim,y="Number",x=r[y],w=x.prototype,C=r.TypeError,S=i("".slice),_=i("".charCodeAt),k=function(t){var e=f(t,"number");return"bigint"==typeof e?e:N(e)},N=function(t){var e,n,a,r,i,s,o,c,u=f(t,"number");if(d(u))throw C("Cannot convert a Symbol value to a number");if("string"==typeof u&&u.length>2)if(u=b(u),43===(e=_(u,0))||45===e){if(88===(n=_(u,2))||120===n)return NaN}else if(48===e){switch(_(u,1)){case 66:case 98:a=2,r=49;break;case 79:case 111:a=8,r=55;break;default:return+u}for(s=(i=S(u,2)).length,o=0;o<s;o++)if((c=_(i,o))<48||c>r)return NaN;return parseInt(i,a)}return+u};if(s(y,!x(" 0o1")||!x("0b1")||x("+0x1"))){for(var I,A=function(t){var e=arguments.length<1?0:x(k(t)),n=this;return l(w,n)&&p((function(){v(n)}))?u(Object(e),n,A):e},F=a?g(x):"MAX_VALUE,MIN_VALUE,NaN,NEGATIVE_INFINITY,POSITIVE_INFINITY,EPSILON,MAX_SAFE_INTEGER,MIN_SAFE_INTEGER,isFinite,isInteger,isNaN,isSafeInteger,parseFloat,parseInt,fromString,range".split(","),O=0;F.length>O;O++)c(x,I=F[O])&&!c(A,I)&&h(A,I,m(x,I));A.prototype=w,w.constructor=A,o(r,y,A)}},c8d2:function(t,e,n){var a=n("5e77").PROPER,r=n("d039"),i=n("5899");t.exports=function(t){return r((function(){return!!i[t]()||"​᠎"!=="​᠎"[t]()||a&&i[t].name!==t}))}},d81d:function(t,e,n){"use strict";var a=n("23e7"),r=n("b727").map;a({target:"Array",proto:!0,forced:!n("1dde")("map")},{map:function(t){return r(this,t,arguments.length>1?arguments[1]:void 0)}})}}]);