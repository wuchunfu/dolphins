(window.webpackJsonp=window.webpackJsonp||[]).push([["chunk-4bd56756"],{"1bf8":function(t,e,a){"use strict";a("84f3")},"5f93":function(t,e,a){},"84f3":function(t,e,a){},9101:function(t,e,a){"use strict";a("be2d")},9406:function(t,e,a){"use strict";a.r(e);var s=a("5530"),i=(a("a9e3"),a("ec1b")),r=a.n(i),n={props:{panelData:{type:Object,required:!0},dateType:{type:Number,required:!0}},data:function(){return{date:["","最近7天","最近30天","最近半年","最近1年","所有"]}},components:{CountTo:r.a},methods:{handleSetLineChartData:function(t){this.$emit("handleSetLineChartData",t)}}},o=n,c=(a("1bf8"),a("2877")),l=Object(c.a)(o,(function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("a-row",{staticClass:"panel-group",attrs:{gutter:5,type:"flex",justify:"space-between"}},[a("a-col",{attrs:{xs:12,sm:12,lg:4}},[a("div",{staticClass:"card-panel",on:{click:function(e){return t.handleSetLineChartData("newVisitis")}}},[a("div",{staticClass:"card-panel-description"},[a("div",{staticClass:"card-panel-title"},[t._v("活跃服务者数")]),a("div",{staticClass:"card-panel-time"},[t._v(t._s(t.date[t.dateType]))]),a("div",[a("count-to",{staticClass:"card-panel-num",attrs:{"start-val":0,"end-val":t.panelData.service_user,duration:2600}}),t._v("/人 ")],1)])])]),a("a-col",{attrs:{xs:12,sm:12,lg:4}},[a("div",{staticClass:"card-panel",on:{click:function(e){return t.handleSetLineChartData("newVisitis")}}},[a("div",{staticClass:"card-panel-description"},[a("div",{staticClass:"card-panel-title"},[t._v("代码提交次数")]),a("div",{staticClass:"card-panel-time"},[t._v(t._s(t.date[t.dateType]))]),a("div",[a("count-to",{staticClass:"card-panel-num",attrs:{"start-val":0,"end-val":t.panelData.commits,duration:2600}}),t._v("/次 ")],1)])])]),a("a-col",{attrs:{xs:12,sm:12,lg:4}},[a("div",{staticClass:"card-panel",on:{click:function(e){return t.handleSetLineChartData("messages")}}},[a("div",{staticClass:"card-panel-description"},[a("div",{staticClass:"card-panel-title"},[t._v("上线发布次数")]),a("div",{staticClass:"card-panel-time"},[t._v(t._s(t.date[t.dateType]))]),a("div",[a("count-to",{staticClass:"card-panel-num",attrs:{"start-val":0,"end-val":t.panelData.release,duration:2600}}),t._v("/次 ")],1)])])]),a("a-col",{attrs:{xs:12,sm:12,lg:4}},[a("div",{staticClass:"card-panel",on:{click:function(e){return t.handleSetLineChartData("purchases")}}},[a("div",{staticClass:"card-panel-description"},[a("div",{staticClass:"card-panel-title"},[t._v("质量风险数")]),a("div",{staticClass:"card-panel-time"},[t._v(t._s(t.date[t.dateType]))]),a("div",[a("count-to",{staticClass:"card-panel-num",attrs:{"start-val":0,"end-val":t.panelData.quality,duration:2600,decimals:0}}),t._v("/个 ")],1)])])]),a("a-col",{attrs:{xs:12,sm:12,lg:4}},[a("div",{staticClass:"card-panel",on:{click:function(e){return t.handleSetLineChartData("shoppings")}}},[a("div",{staticClass:"card-panel-description"},[a("div",{staticClass:"card-panel-title"},[t._v("工程数目总量")]),a("div",{staticClass:"card-panel-time"},[t._v(t._s(t.date[t.dateType]))]),a("div",[a("count-to",{staticClass:"card-panel-num",attrs:{"start-val":0,"end-val":t.panelData.projects,duration:2600,decimals:0}}),t._v("/次 ")],1)])])])],1)}),[],!1,null,"2f51034c",null),d=l.exports,u=a("313e"),p=a.n(u),v=a("b047"),m=a.n(v),h={data:function(){return{$_sidebarElm:null,$_resizeHandler:null}},mounted:function(){var t=this;this.$_resizeHandler=m()((function(){t.chart&&t.chart.resize()}),100),this.$_initResizeEvent(),this.$_initSidebarResizeEvent()},beforeDestroy:function(){this.$_destroyResizeEvent(),this.$_destroySidebarResizeEvent()},activated:function(){this.$_initResizeEvent(),this.$_initSidebarResizeEvent()},deactivated:function(){this.$_destroyResizeEvent(),this.$_destroySidebarResizeEvent()},methods:{$_initResizeEvent:function(){window.addEventListener("resize",this.$_resizeHandler)},$_destroyResizeEvent:function(){window.removeEventListener("resize",this.$_resizeHandler)},$_sidebarResizeHandler:function(t){"width"===t.propertyName&&this.$_resizeHandler()},$_initSidebarResizeEvent:function(){this.$_sidebarElm=document.getElementsByClassName("sidebar-container")[0],this.$_sidebarElm&&this.$_sidebarElm.addEventListener("transitionend",this.$_sidebarResizeHandler)},$_destroySidebarResizeEvent:function(){this.$_sidebarElm&&this.$_sidebarElm.removeEventListener("transitionend",this.$_sidebarResizeHandler)}}};a("817d");var f={mixins:[h],props:{className:{type:String,default:"chart"},width:{type:String,default:"100%"},height:{type:String,default:"300px"},autoResize:{type:Boolean,default:!0},chartData:{type:Object,required:!0}},data:function(){return{chart:null}},watch:{chartData:{deep:!0,handler:function(t){this.setOptions(t)}}},mounted:function(){var t=this,e=setTimeout((function(){t.initChart(),clearTimeout(e)}),20)},beforeDestroy:function(){this.chart&&(this.chart.dispose(),this.chart=null)},methods:{initChart:function(){this.chart=p.a.init(this.$el,"macarons"),this.setOptions(this.chartData)},setOptions:function(t){var e=t.release,a=t.rollback,s=t.dateTime;this.chart.setOption({xAxis:{data:s,axisTick:{show:!1},splitLine:{lineStyle:{type:"dashed",width:1},show:!0},axisLine:{show:!1,lineStyle:{color:"#000",width:1}}},grid:{left:10,right:10,bottom:0,top:40,containLabel:!0},tooltip:{trigger:"axis",axisPointer:{type:"cross"},padding:[5,10]},yAxis:{type:"value",minInterval:1,axisLabel:{formatter:"{value}次"},axisTick:{show:!1},axisLine:{show:!1,lineStyle:{color:"#000",width:1}},splitLine:{lineStyle:{type:"dashed",width:1},show:!0}},legend:{top:"10",right:"40",icon:"circle",data:["发布次数","回滚次数"]},series:[{name:"发布次数",symbol:"none",symbolSize:6,itemStyle:{normal:{color:"#0077FF",lineStyle:{color:"#0077FF",width:2}}},areaStyle:{normal:{color:new p.a.graphic.LinearGradient(0,0,0,1,[{offset:1,color:"rgba(255, 255, 255, .1)"},{offset:0,color:"rgba(0, 119, 255, .5)"}])}},type:"line",smooth:!0,data:e,animationDuration:2800,animationEasing:"cubicInOut"},{name:"回滚次数",type:"line",symbol:"none",symbolSize:6,itemStyle:{normal:{color:"#00D896",lineStyle:{color:"#00D896",width:2}}},areaStyle:{normal:{color:new p.a.graphic.LinearGradient(0,0,0,1,[{offset:1,color:"rgba(255, 255, 255, .1)"},{offset:0,color:"rgba(22, 247, 103, .5)"}])}},data:a,smooth:!0,animationDuration:2800,animationEasing:"quadraticOut"}]})}}},_=f,y=Object(c.a)(_,(function(){var t=this,e=t.$createElement;return(t._self._c||e)("div",{class:t.className,style:{height:t.height,width:t.width}})}),[],!1,null,null,null),w=y.exports;a("d81d"),a("b0c0"),a("817d");var C={mixins:[h],props:{className:{type:String,default:"chart"},width:{type:String,default:"100%"},height:{type:String,default:"300px"},chartData:{type:Object/Array,required:!0},radius:{type:Array,default:function(){return[40,90]}},title:{type:String,default:""}},data:function(){return{chart:null}},watch:{chartData:{deep:!0,handler:function(t){this.setOptions(t)}}},mounted:function(){var t=this;this.$nextTick((function(){t.initChart()}))},beforeDestroy:function(){this.chart&&(this.chart.dispose(),this.chart=null)},methods:{initChart:function(){this.chart=p.a.init(this.$el,"macarons"),this.setOptions(this.chartData)},setOptions:function(){var t=arguments.length>0&&void 0!==arguments[0]?arguments[0]:[];this.chart.setOption({tooltip:{trigger:"item",formatter:"{b} : {c} ({d}%)",confirm:!0},legend:{left:"center",bottom:"10",data:t.map((function(t){return t.name}))},series:[{name:this.title,type:"pie",radius:this.radius,center:["50%","38%"],data:t,animationEasing:"cubicInOut",animationDuration:2600}]})}}},g=C,b=Object(c.a)(g,(function(){var t=this,e=t.$createElement;return(t._self._c||e)("div",{class:t.className,style:{height:t.height,width:t.width}})}),[],!1,null,null,null),x=b.exports,D=[{title:"应用名",dataIndex:"app"},{title:"版本号",dataIndex:"version"},{title:"创建时间",dataIndex:"createtime"},{title:"状态",dataIndex:"status",scopedSlots:{customRender:"status"}}],k={0:{status:"default",text:"待检查"},1:{status:"processing",text:"检查中"},2:{status:"processing",text:"待发布"},3:{status:"processing",text:"发布中"},4:{status:"success",text:"已发布"},5:{status:"warning",text:"回滚中"},6:{status:"warning",text:"已回滚"},7:{status:"default",text:"发布异常"},8:{status:"error",text:"项目异常"}},S={filters:{statusFilter:function(t){return k[t].text},statusTypeFilter:function(t){return k[t].status}},props:{list:{type:Array,required:!0}},data:function(){return{columns:D}},methods:{},created:function(){},mounted:function(){},beforeCreate:function(){},beforeMount:function(){},beforeUpdate:function(){},updated:function(){},beforeDestroy:function(){},destroyed:function(){},activated:function(){}},$=S,R=(a("9101"),Object(c.a)($,(function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",{staticClass:"todo-box"},[a("a-table",{ref:"table",attrs:{size:"middle",columns:t.columns,dataSource:t.list,pagination:!1,scroll:{y:0},rowKey:function(t,e){return e}},scopedSlots:t._u([{key:"status",fn:function(e){return a("span",{},[a("a-badge",{attrs:{status:t._f("statusTypeFilter")(e),text:t._f("statusFilter")(e)}})],1)}}])})],1)}),[],!1,null,"79938e43",null)),E=R.exports,F=a("365c"),T=a("985d"),z=(a("c1df"),{0:{status:"processing",text:"待创建"},1:{status:"processing",text:"创建中"},2:{status:"success",text:"已创建"},3:{status:"error",text:"已失效"}}),O={0:{status:"default",text:"等待中"},1:{status:"processing",text:"构建中"},2:{status:"processing",text:"待发布"},3:{status:"processing",text:"发布中"},4:{status:"success",text:"已发布"},5:{status:"warning",text:"回滚中"},6:{status:"warning",text:"已回滚"},7:{status:"error",text:"发布异常"}},L={0:{color:"#52c41a",icon:"up-circle"},1:{color:"#A9A9A9",icon:"minus-circle"},2:{color:"#FF4500",icon:"down-circle"}},j={name:"dashboard",components:{rocket:T.j,PanelGroup:d,LineChart:w,PieChart:x,TodoList:E},filters:{statusFilter:function(t,e){return e?O[t].text:z[t].text},statusTypeFilter:function(t,e){return e?O[t].status:z[t].status}},data:function(){return{compareStatusMap:L,quickList:[{title:"发布上线",explain:"快速发布线上业务",icon:T.c,color:"#0073ff",path:"/release"},{title:"项目工程",explain:"统一项目工程管理",icon:T.b,color:"#00d896",path:"/project"},{title:"集群管理",explain:"快速创建流水线集群",icon:T.a,color:"#6236ff",path:"/cluster"},{title:"服务广场",explain:"寻找技术问题解决方案",icon:T.d,color:"#ffc052",path:"/cluster"}],form:{startTime:void 0,endTime:void 0},headerData:{project:{total:0,list:[]},release:{total:0,list:[]},dayRelease:{total:0,compare:0},weekRelease:{total:0,compare:0},weekRisk:{total:0,compare:0}},chartData:{dateTime:[],release:[],rollback:[]},loading:!1}},computed:{},methods:{handleSetLineChartData:function(t){},initData:function(){var t=this;this.loading=!0,Object(F.l)().then((function(e){var a=e.status,i=e.data,r=e.msg;1===a?t.headerData=Object(s.a)({},i):t.$messages.error(r)})).catch((function(t){})),Object(F.k)().then((function(e){var a=e.status,i=e.data,r=e.msg;1===a?t.chartData=Object(s.a)({},i):t.$messages.error(r)})).catch((function(t){}))},refreshDate:function(){this.initData()},selectdate:function(t,e){var a=this;this.form.startTime=e[0]||void 0,this.form.endTime=e[1]||void 0,Object(F.k)(Object(s.a)({},this.form)).then((function(t){var e=t.status,i=t.data,r=t.msg;1===e?a.chartData=Object(s.a)({},i):a.$messages.error(r)})).catch((function(t){}))},clickMenu:function(t){this.$router.push({path:t}).catch((function(t){}))},compareFilter:function(t,e){return t>0?1==e?this.compareStatusMap[0].color:this.compareStatusMap[0].icon:0==t?1==e?this.compareStatusMap[1].color:this.compareStatusMap[1].icon:1==e?this.compareStatusMap[2].color:this.compareStatusMap[2].icon}},created:function(){var t=this;this.$nextTick((function(){t.initData()}))},mounted:function(){},beforeCreate:function(){},beforeMount:function(){},beforeUpdate:function(){},updated:function(){},beforeDestroy:function(){},destroyed:function(){},activated:function(){}},M=j,q=(a("a860"),Object(c.a)(M,(function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",{staticClass:"dashboard-box"},[a("a-row",{attrs:{type:"flex",justify:"center"}},[a("h1",{staticClass:"dashboard-header"},[t._v("快捷入口")]),a("a-col",{attrs:{span:24}},[a("a-row",{staticClass:"quick-entry-list",attrs:{gutter:20}},t._l(t.quickList,(function(e,s){return a("a-col",{key:s,staticClass:"mb20 quick-entry-col",attrs:{xs:24,sm:24,lg:12,xl:6,xxl:6},on:{click:function(a){return t.clickMenu(e.path)}}},[a("div",{staticClass:"quick-entry-item"},[a("div",{staticClass:"quick-entry-item-svg"},[a("a-icon",{style:"color:"+e.color,attrs:{component:e.icon}})],1),a("div",{staticClass:"quick-entry-text"},[a("div",{staticClass:"quick-entry-item-title"},[t._v(t._s(e.title))]),a("div",{staticClass:"quick-entry-item-explain"},[t._v(t._s(e.explain))])])])])})),1),a("a-row",{staticClass:"overview",attrs:{gutter:20}},[a("a-col",{attrs:{xs:24,sm:24,lg:12}},[a("div",{staticClass:"mb30 overview-item"},[a("div",[a("div",{staticClass:"overview-item-count"},[t._v("总工程数")]),a("div",[a("span",{staticClass:"overview-item-num"},[t._v(t._s(t.headerData.project.total))]),t._v("个 ")])]),a("div",{staticClass:"overview-item-list"},[a("div",{staticClass:"overview-item-title"},[t._v("最近创建工程排列")]),a("ul",{staticClass:"p0 overview-item-ul"},t._l(t.headerData.project.list.slice(0,3),(function(e,s){return a("li",{key:s,staticClass:"overview-item-li"},[a("div",{staticClass:"item-li-name"},[t._v(t._s(e.name))]),a("div",{staticClass:"item-li-status"},[a("a-badge",{attrs:{status:t._f("statusTypeFilter")(e.status),text:t._f("statusFilter")(e.status)}})],1),a("div",{staticClass:"item-li-time"},[t._v(t._s(t._f("moment")(e.time,"YYYY-MM-DD")))])])})),0)])])]),a("a-col",{attrs:{xs:24,sm:24,lg:12}},[a("div",{staticClass:"mb30 overview-item"},[a("div",[a("div",{staticClass:"overview-item-count"},[t._v("总发布数")]),a("div",[a("span",{staticClass:"overview-item-num"},[t._v(t._s(t.headerData.release.total))]),t._v("次 ")])]),a("div",{staticClass:"overview-item-list"},[a("div",{staticClass:"overview-item-title"},[t._v("最近发布的业务")]),a("ul",{staticClass:"p0 overview-item-ul"},t._l(t.headerData.release.list.slice(0,3),(function(e,s){return a("li",{key:s,staticClass:"overview-item-li"},[a("span",{staticClass:"item-li-name"},[t._v(t._s(e.name))]),a("span",{staticClass:"item-li-status"},[a("a-badge",{attrs:{status:t._f("statusTypeFilter")(e.status,!0),text:t._f("statusFilter")(e.status,!0)}})],1),a("span",{staticClass:"item-li-time"},[t._v(t._s(t._f("moment")(e.time,"YYYY-MM-DD")))])])})),0)])])]),a("a-col",{attrs:{xs:24,sm:24,lg:8}},[a("div",{staticClass:"mb30 overview-item"},[a("div",[a("div",{staticClass:"overview-item-count"},[t._v("今日新发布业务")]),a("div",[a("span",{staticClass:"overview-item-num"},[t._v(t._s(t.headerData.dayRelease.total))]),t._v("次 ")])]),a("div",{staticClass:"overview-item-ringgit"},[a("div",{staticClass:"overview-item-title"},[t._v(" 较上日 "),a("a-icon",{attrs:{type:t.compareFilter(t.headerData.dayRelease.compare,0),theme:"twoTone","two-tone-color":t.compareFilter(t.headerData.dayRelease.compare,1)}})],1),a("div",{staticClass:"overview-item-progress"},[a("a-progress",{attrs:{"stroke-linecap":"square",type:"circle",gapPosition:"right",strokeWidth:13,strokeColor:t.compareFilter(t.headerData.dayRelease.compare,1),percent:Math.abs(+t.headerData.dayRelease.compare),width:104},scopedSlots:t._u([{key:"format",fn:function(e){return[a("span",{style:{color:t.compareFilter(t.headerData.dayRelease.compare,1)}},[t._v(t._s(e)+"%")])]}}])})],1)])])]),a("a-col",{attrs:{xs:24,sm:24,lg:8}},[a("div",{staticClass:"mb30 overview-item"},[a("div",[a("div",{staticClass:"overview-item-count"},[t._v("本周新发布业务")]),a("div",[a("span",{staticClass:"overview-item-num"},[t._v(t._s(t.headerData.weekRelease.total))]),t._v("次 ")])]),a("div",{staticClass:"overview-item-ringgit"},[a("div",{staticClass:"overview-item-title"},[t._v(" 较上周 "),a("a-icon",{attrs:{type:t.compareFilter(t.headerData.weekRelease.compare,0),theme:"twoTone","two-tone-color":t.compareFilter(t.headerData.weekRelease.compare,1)}})],1),a("div",{staticClass:"overview-item-progress"},[a("a-progress",{attrs:{"stroke-linecap":"square",type:"circle",gapPosition:"right",strokeWidth:13,strokeColor:t.compareFilter(t.headerData.weekRelease.compare,1),percent:Math.abs(+t.headerData.weekRelease.compare),width:104},scopedSlots:t._u([{key:"format",fn:function(e){return[a("span",{style:{color:t.compareFilter(t.headerData.weekRelease.compare,1)}},[t._v(t._s(e)+"%")])]}}])})],1)])])]),a("a-col",{attrs:{xs:24,sm:24,lg:8}},[a("div",{staticClass:"mb30 overview-item"},[a("div",[a("div",{staticClass:"overview-item-count"},[t._v("本周发现风险")]),a("div",[a("span",{staticClass:"overview-item-num"},[t._v(t._s(t.headerData.weekRisk.total))]),t._v("个 ")])]),a("div",{staticClass:"overview-item-ringgit"},[a("div",{staticClass:"overview-item-title"},[t._v(" 较上周 "),a("a-icon",{attrs:{type:t.compareFilter(t.headerData.weekRisk.compare,0),theme:"twoTone","two-tone-color":t.compareFilter(t.headerData.weekRisk.compare,1)}})],1),a("div",{staticClass:"overview-item-progress"},[a("a-progress",{attrs:{"stroke-linecap":"square",type:"circle",gapPosition:"right",strokeWidth:13,strokeColor:t.compareFilter(t.headerData.weekRisk.compare,1),percent:Math.abs(+t.headerData.weekRisk.compare),width:104},scopedSlots:t._u([{key:"format",fn:function(e){return[a("span",{style:{color:t.compareFilter(t.headerData.weekRisk.compare,1)}},[t._v(t._s(e)+"%")])]}}])})],1)])])]),a("a-col",{attrs:{span:24}},[a("div",{staticClass:"mb30 release-trends"},[a("div",{staticClass:"release-trends-header"},[a("div",{staticClass:"release-trends-title"},[t._v("发布总数趋势")]),a("a-range-picker",{attrs:{format:"YYYY-MM-DD"},on:{change:t.selectdate}},[a("a-icon",{attrs:{slot:"suffixIcon",type:"calendar"},slot:"suffixIcon"})],1)],1),a("line-chart",{attrs:{"chart-data":t.chartData}})],1)])],1)],1)],1)],1)}),[],!1,null,"8b2843ba",null));e.default=q.exports},a860:function(t,e,a){"use strict";a("5f93")},be2d:function(t,e,a){}}]);