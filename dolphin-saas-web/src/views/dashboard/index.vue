<template>
  <div class="dashboard-box">
    <a-row type="flex" justify="center">
      <h1 class="dashboard-header">快捷入口</h1>
      <a-col :span="24">
        <a-row class="quick-entry-list" :gutter="20">
          <a-col
            :xs="24"
            :sm="24"
            :lg="12"
            :xl="6"
            :xxl="6"
            class="mb20 quick-entry-col"
            v-for="(d,i) in quickList"
            :key="i"
            @click="clickMenu(d.path)"
          >
            <div class="quick-entry-item">
              <div class="quick-entry-item-svg">
                <a-icon :component="d.icon" :style="`color:${d.color}`" />
              </div>
              <div class="quick-entry-text">
                <div class="quick-entry-item-title">{{d.title}}</div>
                <div class="quick-entry-item-explain">{{d.explain}}</div>
              </div>
            </div>
          </a-col>
        </a-row>
        <a-row :gutter="20" class="overview">
          <a-col :xs="24" :sm="24" :lg="12">
            <div class="mb30 overview-item">
              <div>
                <div class="overview-item-count">总工程数</div>
                <div>
                  <span class="overview-item-num">{{headerData.project.total}}</span>个
                </div>
              </div>
              <div class="overview-item-list">
                <div class="overview-item-title">最近创建工程排列</div>
                <ul class="p0 overview-item-ul">
                  <li
                    class="overview-item-li"
                    v-for="(d,i) in headerData.project.list.slice(0,3)"
                    :key="i"
                  >
                    <div class="item-li-name">{{d.name}}</div>
                    <div class="item-li-status">
                      <a-badge
                        :status="d.status | statusTypeFilter"
                        :text="d.status | statusFilter"
                      />
                    </div>
                    <div class="item-li-time">{{d.time|moment('YYYY-MM-DD')}}</div>
                  </li>
                </ul>
              </div>
            </div>
          </a-col>
          <a-col :xs="24" :sm="24" :lg="12">
            <div class="mb30 overview-item">
              <div>
                <div class="overview-item-count">总发布数</div>
                <div>
                  <span class="overview-item-num">{{headerData.release.total}}</span>次
                </div>
              </div>
              <div class="overview-item-list">
                <div class="overview-item-title">最近发布的业务</div>
                <ul class="p0 overview-item-ul">
                  <li
                    class="overview-item-li"
                    v-for="(d,i) in headerData.release.list.slice(0,3)"
                    :key="i"
                  >
                    <span class="item-li-name">{{d.name}}</span>
                    <span class="item-li-status">
                      <a-badge
                        :status="d.status | statusTypeFilter(true)"
                        :text="d.status | statusFilter(true)"
                      />
                    </span>
                    <span class="item-li-time">{{d.time|moment('YYYY-MM-DD')}}</span>
                  </li>
                </ul>
              </div>
            </div>
          </a-col>
          <a-col :xs="24" :sm="24" :lg="8">
            <div class="mb30 overview-item">
              <div>
                <div class="overview-item-count">今日新发布业务</div>
                <div>
                  <span class="overview-item-num">{{headerData.dayRelease.total}}</span>次
                </div>
              </div>
              <div class="overview-item-ringgit">
                <div class="overview-item-title">
                  较上日
                  <a-icon
                    :type="compareFilter(headerData.dayRelease.compare,0)"
                    theme="twoTone"
                    :two-tone-color="compareFilter(headerData.dayRelease.compare,1)"
                  />
                </div>
                <div class="overview-item-progress">
                  <a-progress
                    stroke-linecap="square"
                    type="circle"
                    gapPosition="right"
                    :strokeWidth="13"
                    :strokeColor="compareFilter(headerData.dayRelease.compare,1)"
                    :percent="Math.abs(+headerData.dayRelease.compare)"
                    :width="104"
                  >
                    <template #format="percent">
                      <span
                        :style="{color:compareFilter(headerData.dayRelease.compare,1)}"
                      >{{ percent }}%</span>
                    </template>
                  </a-progress>
                </div>
              </div>
            </div>
          </a-col>
          <a-col :xs="24" :sm="24" :lg="8">
            <div class="mb30 overview-item">
              <div>
                <div class="overview-item-count">本周新发布业务</div>
                <div>
                  <span class="overview-item-num">{{headerData.weekRelease.total}}</span>次
                </div>
              </div>
              <div class="overview-item-ringgit">
                <div class="overview-item-title">
                  较上周
                  <a-icon
                    :type="compareFilter(headerData.weekRelease.compare,0)"
                    theme="twoTone"
                    :two-tone-color="compareFilter(headerData.weekRelease.compare,1)"
                  />
                </div>
                <div class="overview-item-progress">
                  <a-progress
                    stroke-linecap="square"
                    type="circle"
                    gapPosition="right"
                    :strokeWidth="13"
                    :strokeColor="compareFilter(headerData.weekRelease.compare,1)"
                    :percent="Math.abs(+headerData.weekRelease.compare)"
                    :width="104"
                  >
                    <template #format="percent">
                      <span
                        :style="{color:compareFilter(headerData.weekRelease.compare,1)}"
                      >{{ percent }}%</span>
                    </template>
                  </a-progress>
                </div>
              </div>
            </div>
          </a-col>
          <a-col :xs="24" :sm="24" :lg="8">
            <div class="mb30 overview-item">
              <div>
                <div class="overview-item-count">本周发现风险</div>
                <div>
                  <span class="overview-item-num">{{headerData.weekRisk.total}}</span>个
                </div>
              </div>
              <div class="overview-item-ringgit">
                <div class="overview-item-title">
                  较上周
                  <a-icon
                    :type="compareFilter(headerData.weekRisk.compare,0)"
                    theme="twoTone"
                    :two-tone-color="compareFilter(headerData.weekRisk.compare,1)"
                  />
                </div>
                <div class="overview-item-progress">
                  <a-progress
                    stroke-linecap="square"
                    type="circle"
                    gapPosition="right"
                    :strokeWidth="13"
                    :strokeColor="compareFilter(headerData.weekRisk.compare,1)"
                    :percent="Math.abs(+headerData.weekRisk.compare)"
                    :width="104"
                  >
                    <template #format="percent">
                      <span
                        :style="{color:compareFilter(headerData.weekRisk.compare,1)}"
                      >{{ percent }}%</span>
                    </template>
                  </a-progress>
                </div>
              </div>
            </div>
          </a-col>
          <a-col :span="24">
            <div class="mb30 release-trends">
              <div class="release-trends-header">
                <div class="release-trends-title">发布总数趋势</div>
                <a-range-picker format="YYYY-MM-DD" @change="selectdate">
                  <a-icon slot="suffixIcon" type="calendar" />
                </a-range-picker>
              </div>
              <line-chart :chart-data="chartData" />
            </div>
          </a-col>
        </a-row>
      </a-col>
    </a-row>
  </div>
</template>

<script>
import PanelGroup from './components/PanelGroup'
import LineChart from './components/LineChart'
import PieChart from './components/PieChart'
import TodoList from './components/TodoList.vue'

import { dashboardHeader, dashboardBottom } from '@/api/index'
import { rocket, Dcluster, Dproject, Drelease, Dserve } from '@/icons'
import moment from 'moment'
//工程状态
const projectStatusMap = {
  0: { status: 'processing', text: '待创建' },
  1: { status: 'processing', text: '创建中' },
  2: { status: 'success', text: '已创建' },
  3: { status: 'error', text: '已失效' },
}
//发布状态
const releaseStatusMap = {
  0: { status: 'default', text: '等待中' },
  1: { status: 'processing', text: '构建中' },
  2: { status: 'processing', text: '待发布' },
  3: { status: 'processing', text: '发布中' },
  4: { status: 'success', text: '已发布' },
  5: { status: 'warning', text: '回滚中' },
  6: { status: 'warning', text: '已回滚' },
  7: { status: 'error', text: '发布异常' },
}
const compareStatusMap = {
  0: { color: '#52c41a', icon: 'up-circle' },
  1: { color: '#A9A9A9', icon: 'minus-circle' },
  2: { color: '#FF4500', icon: 'down-circle' },
}
export default {
  name: 'dashboard',
  components: {
    rocket,
    PanelGroup,
    LineChart,
    PieChart,
    TodoList,
  },
  filters: {
    statusFilter(status, type) {
      return type
        ? releaseStatusMap[status].text
        : projectStatusMap[status].text
    },
    statusTypeFilter(status, type) {
      return type
        ? releaseStatusMap[status].status
        : projectStatusMap[status].status
    },
  },
  data() {
    return {
      compareStatusMap,
      quickList: [
        {
          title: '发布上线',
          explain: '快速发布线上业务',
          icon: Drelease,
          color: '#0073ff',
          path: '/release',
        },
        {
          title: '项目工程',
          explain: '统一项目工程管理',
          icon: Dproject,
          color: '#00d896',
          path: '/project',
        },
        {
          title: '集群管理',
          explain: '快速创建流水线集群',
          icon: Dcluster,
          color: '#6236ff',
          path: '/cluster',
        },
        {
          title: '服务广场',
          explain: '寻找技术问题解决方案',
          icon: Dserve,
          color: '#ffc052',
          path: '/cluster',
        },
      ],
      form: { startTime: undefined, endTime: undefined },
      headerData: {
        project: { total: 0, list: [] },
        release: { total: 0, list: [] },
        dayRelease: { total: 0, compare: 0 },
        weekRelease: { total: 0, compare: 0 },
        weekRisk: { total: 0, compare: 0 },
      },
      chartData: {
        dateTime: [],
        release: [],
        rollback: [],
      },
      loading: false,
    }
  },
  computed: {},
  methods: {
    handleSetLineChartData(type) {
      // this.lineChartData = lineChartData[type];
    },
    //初始化数据
    initData() {
      this.loading = true
      dashboardHeader()
        .then(({ status, data, msg }) => {
          if (status === 1) {
            this.headerData = { ...data }
          } else {
            this.$messages.error(msg)
          }
        })
        .catch((err) => {})
      dashboardBottom()
        .then(({ status, data, msg }) => {
          if (status === 1) {
            this.chartData = { ...data }
          } else {
            this.$messages.error(msg)
          }
        })
        .catch((err) => {})
    },
    refreshDate() {
      // console.log("form.date", parseTime(this.form, "{y}-{m}-{d}"));
      this.initData()
    },
    selectdate(date, dateString) {
      this.form.startTime = dateString[0] || undefined
      this.form.endTime = dateString[1] || undefined
      dashboardBottom({ ...this.form })
        .then(({ status, data, msg }) => {
          if (status === 1) {
            this.chartData = { ...data }
          } else {
            this.$messages.error(msg)
          }
        })
        .catch((err) => {})
    },
    clickMenu(path) {
      this.$router.push({ path }).catch((err) => {})
    },
    compareFilter(val, type) {
      if (val > 0)
        return type == 1
          ? this.compareStatusMap[0].color
          : this.compareStatusMap[0].icon
      else if (val == 0)
        return type == 1
          ? this.compareStatusMap[1].color
          : this.compareStatusMap[1].icon
      else
        return type == 1
          ? this.compareStatusMap[2].color
          : this.compareStatusMap[2].icon
    },
  },
  created() {
    this.$nextTick(() => {
      this.initData()
    })
  },
  //生命周期 - 挂载完成（可以访问DOM元素）
  mounted() {},
  beforeCreate() {}, //生命周期 - 创建之前
  beforeMount() {}, //生命周期 - 挂载之前
  beforeUpdate() {}, //生命周期 - 更新之前
  updated() {}, //生命周期 - 更新之后
  beforeDestroy() {}, //生命周期 - 销毁之前
  destroyed() {}, //生命周期 - 销毁完成
  activated() {}, //如果页面有keep-alive缓存功能，这个函数会触发
}
</script>
<style lang="less" scoped>
.dashboard-box {
  .dashboard-header {
    width: 100%;
    color: rgba(0, 0, 0, 0.85);
    font-weight: 500;
    line-height: 35px;
    font-size: 16px;
    color: #222c64;
    text-align: left;
  }
  .quick-entry {
    &-title {
      color: #282d3c;
      font-size: 16px;
      font-weight: 700;
      margin-bottom: 24px;
    }
    &-item {
      display: flex;
      height: 100%;
      padding: 16px !important;
      border-radius: 8px;
      box-sizing: border-box;
      border: 1px solid #eaebee;
      &:hover {
        background: #f5f8fb;
        transition: all 0.4s linear;
      }
      &-explain {
        margin-top: 6px;
        font-size: 12px;
      }
      &-svg {
        width: 48px;
        height: 48px;
        min-height: 48px;
        border-radius: 8px;
        overflow: hidden;
        display: flex;
        justify-content: center;
        align-items: center;
        margin-right: 10px;
        background: #ffffff;
        box-shadow: 0px 0px 10px 0px rgba(63, 71, 122, 0.09);
        i {
          font-size: 24px;
          color: #2684ff;
        }
      }
      &-text {
        flex: 1;
      }
    }
  }
  .overview {
    &-item {
      display: flex;
      flex-wrap: wrap;
      justify-content: space-between;
      padding: 24px;
      border: 1px solid #eaebee;
      color: #878a95;
      font-size: 14px;
      &-count {
        margin-bottom: 32px;
      }
      &-list {
        height: 132px;
        min-width: 256px;
        // width: 100%;
        .overview-item-title {
          margin-bottom: 16px;
        }
        .overview-item-ul {
          height: 100px;
          overflow-y: auto;
          &::-webkit-scrollbar {
            width: 7px;
            height: 7px;
            background-color: #f5f5f5;
          }

          /*定义滚动条轨道 内阴影+圆角*/
          &::-webkit-scrollbar-track {
            // box-shadow: inset 0 0 6px rgba(0, 0, 0, 0.3);
            // -webkit-box-shadow: inset 0 0 6px rgba(0, 0, 0, 0.3);
            border-radius: 10px;
            background-color: #f5f5f5;
          }

          /*定义滑块 内阴影+圆角*/
          &::-webkit-scrollbar-thumb {
            border-radius: 10px;
            // box-shadow: inset 0 0 6px rgba(0, 0, 0, 0.1);
            // -webkit-box-shadow: inset 0 0 6px rgba(0, 0, 0, 0.1);
            background-color: #c8c8c8;
          }
        }
        .overview-item-li {
          display: flex;
          justify-content: space-between;
          padding: 6px 0;
          padding-right: 4px;
          color: #000;
          .item-li-name {
            flex: 1;
            text-align: left;
          }
          .item-li-status {
            padding: 0 6px;
            width: 82px;
          }
          .item-li-time {
            color: #878a95;
          }
        }
      }
      &-num {
        font-size: 36px;
        color: #282d3c;
        line-height: 42px;
        font-family: Helvetica Neue;
        font-style: normal;
        font-weight: 500;
      }
      &-ringgit {
        width: 160px;
        .overview-item-title {
          margin-bottom: 8px;
        }
        .overview-item-progress {
          margin-top: 24px;
          width: 124px;
          height: 124px;
          border: 10px solid #e2eefd;
          border-radius: 50%;
        }
      }
    }
  }
  .release-trends {
    padding: 24px;
    border: 1px solid #eaebee;
    color: #878a95;
    font-size: 14px;
    &-header {
      display: flex;
      justify-content: space-between;
      color: #878a95;
    }
  }
}
</style>