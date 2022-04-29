<template>
  <div class="beginnertask-box">
    <a-row type="flex" justify="center">
      <a-col :xs="24" :sm="24" :lg="24" :xl="20">
        <a-row class="task-top mb30">
          <a-col :xs="24" :sm="10" :lg="14" :xl="16" class="task-top-title">
            <span>新手引导</span>
          </a-col>
          <a-col :xs="18" :sm="14" :lg="10" :xl="8">
            <div class="text-right">
              已完成
              <span class="schedule">{{completeNum}} / {{taskTotal}}</span>
            </div>
            <div class="progress-list mt10">
              <div v-for="(d,index) in taskList" :key="index" :class="d.state?'progress-finish':''"></div>
            </div>
          </a-col>
        </a-row>
        <div class="task-list">
          <div :loading="loading" v-for="(d,index) in taskList" :key="index" class="mb20 task-item">
            <div class="task-img">
              <img :src="d.taskimg" draggable="false" />
            </div>
            <div>
              <div class="tast-item-title">
                <span class="mr20">{{d.title}}</span>
                <a-tooltip :title="d.explain">
                  <a-icon v-if="!d.state" type="question-circle-o" :style="{color:'#878a95'}" />
                </a-tooltip>
                <a-icon
                  v-if="d.state"
                  type="check-circle"
                  theme="twoTone"
                  two-tone-color="#52c41a"
                />
              </div>
              <div class="tast-item-content">{{d.content}}</div>
              <div class="tast-item-text">
                <div>
                  <router-link :to="{name:d.linkUrl}">
                    <a-icon :type="d.icon" class="tast-icon mr5" />
                    <a-button type="link" class="help-link p0">{{d.linkText}}</a-button>
                  </router-link>
                </div>
                <div>
                  <router-link :to="{name:d.helpUrl}">
                    <a-icon type="file-search" class="tast-icon mr5" />
                    <a-button type="link" class="help-link p0">帮助文档</a-button>
                  </router-link>
                </div>
              </div>
            </div>
          </div>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script>
import { newComerTask } from '@/api/index'
import { rocket } from '@/icons'
import Tasks1 from '@/assets/images/Tasks-1.png'
import Tasks2 from '@/assets/images/Tasks-2.png'
import Tasks3 from '@/assets/images/Tasks-3.png'
import Tasks4 from '@/assets/images/Tasks-4.png'
export default {
  name: 'RegisterResult',
  components: { rocket },
  data() {
    return {
      loading: false,
      taskList: [
        {
          title: '关联云AK服务',
          explain: '托管您的AK密钥',
          content: '登陆您的云服务商，创建AK并托管到平台中。',
          linkText: '初始化第一个AK密钥',
          linkUrl: 'cloudConfig',
          taskimg: Tasks1,
          helpUrl: '#',
          icon: 'link',
          state: false,
        },
        {
          title: '创建部署集群',
          explain: '部署容器集群及配套服务',
          content: '根据您的需要，选择一个规模的集群，在指定的云上部署',
          linkText: '初始化第一个集群',
          linkUrl: 'cluster',
          taskimg: Tasks2,
          helpUrl: '#',
          icon: 'switcher',
          state: false,
        },
        {
          title: '创建工程服务',
          explain: '创建开发工程',
          content: '实现业务需求，需要创建一个工程，用于线上使用',
          linkText: '初始化第一个工程',
          linkUrl: 'project',
          taskimg: Tasks3,
          helpUrl: '#',
          icon: 'plus-circle',
          state: false,
        },
        {
          title: '创建工程发布',
          explain: '创建工程发布',
          content: '工程创建完 ，代码推送完远端仓库后，得真实的开始业务发布了',
          linkText: '发布第一个业务工程',
          linkUrl: 'release',
          taskimg: Tasks4,
          helpUrl: '#',
          icon: 'star',
          state: false,
        },
      ],
      taskTotal: 4,
      completeNum: 0,
    }
  },
  created() {
    this.init()
  },
  methods: {
    init() {
      newComerTask()
        .then(({ status, data, msg }) => {
          if (status === 1) {
            let arr = ['vendors', 'cluster', 'engineer', 'release']
            let completeNum = 0
            this.taskList = this.taskList.map((item, i) => {
              item.state = Boolean(data[arr[i]])
              item.state ? completeNum++ : ''
              return item
            })
            this.completeNum = completeNum
          } else {
            this.$message.error(msg)
          }
        })
        .catch((error) => {
          console.error(error)
        })
    },
  },
}
</script>


<style lang="less" scoped>
.beginnertask-box {
  .task-top {
    .task-top-title {
      color: rgba(0, 0, 0, 0.85);
      font-weight: 500;
      line-height: 35px;
      font-size: 16px;
      color: #222c64;
    }
    .text-right {
      text-align: right;
    }
    .schedule {
      font-size: 20px;
      font-weight: 600;
      color: #222c64;
      line-height: 35px;
    }
    .progress-list {
      display: flex;
      justify-content: space-between;
      div {
        width: 20%;
        height: 6px;
        background: #eaebee;
        border-radius: 8px;
      }
      .progress-finish {
        background: #0073ff;
      }
    }
  }
  .task-item {
    display: flex;
    flex-wrap: wrap;
    min-height: 210px;
    border-radius: 10px;
    border: 1px solid #ebedf0;
    padding: 20px;
    padding-top: 30px;
    box-sizing: border-box;
    .task-img {
      display: flex;
      width: 220px;
      padding: 20px 0;
      margin: auto 0;
      justify-content: center;
      align-items: center;
    }
    .tast-item-title {
      font-weight: 500;
      font-size: 16px;
      margin-bottom: 12px;
      display: flex;
      align-items: center;
      color: #222c64;
    }
    .tast-item-content {
      color: #222c64;
      font-size: 14px;
      margin-bottom: 70px;
    }
    .tast-item-text {
      display: flex;
      flex-wrap: wrap;
      & > div:first-child {
        margin-right: 50px;
      }
    }
    .tast-icon {
      color: #0073ff;
      border-radius: 50px;
      padding: 5px;
      background: rgba(0, 115, 255, 0.1);
    }
    .help-link {
      color: #0073ff;
    }
  }
}
</style>
