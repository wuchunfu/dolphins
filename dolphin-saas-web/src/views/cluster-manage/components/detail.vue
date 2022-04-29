<template>
  <div>
    <!-- <a-divider orientation="left">
      集群初始化
    </a-divider>
    <div>资源利用率</div>
    <a-progress
      type="dashboard"
      :stroke-color="{
        '0%': '#FF0000',
        '30%':'#FFA500',
        '100%': '#87d068',
      }"
      :percent="100"
    >
      <template #format="percent">
        <span style="color: red">{{ percent }}%</span>
      </template>
    </a-progress>-->
    <a-divider orientation="left">基础信息</a-divider>
    <a-descriptions size="small" :column="isMobile ? 1 : 2" layout="vertical" bordered>
      <a-descriptions-item label="集群别名">{{clusterInfo.clusterName||'无'}}</a-descriptions-item>
      <a-descriptions-item label="集群ID">{{clusterInfo.clusterId||'无'}}</a-descriptions-item>
      <a-descriptions-item label="创建时间">{{clusterInfo.clusterCreatetime||'无'}}</a-descriptions-item>
      <a-descriptions-item label="最后更新时间">{{clusterInfo.clusterUpdatetime||'无'}}</a-descriptions-item>
    </a-descriptions>
    <a-divider orientation="left" class="relative">
      集群配置服务
      <div class="deploySwitch">
        <div :class="types==1?'text-blue':''">普通用户</div>
        <a-switch :checked="!Boolean(types)" @change="switchTypes" class="mx10" />
        <div :class="types==0?'text-blue':''">技术专家</div>
      </div>
    </a-divider>
    <a-table
      ref="table"
      size="middle"
      :columns="columns"
      :dataSource="deploylist"
      :loading="listLoading"
      :pagination="false"
      :rowKey="(record,index)=>{return index}"
    >
      <span slot="clusterConfigInfo" slot-scope="text,record">
        <span class v-if="text.length>60&&!unfoldArr.includes(record.id)">
          {{text.substring(0,60)}}
          <span class="text-blue cursor-pointer" @click="unfold(record.id)">
            <a-icon type="down" class="mr5" />展开全部
          </span>
        </span>
        <span class v-if="text.length>60&&unfoldArr.includes(record.id)">
          {{text}}
          <span class="text-blue cursor-pointer" @click="fold(record.id)">
            <a-icon type="down" class="mr5" />收起全部
          </span>
        </span>
        <span v-if="text.length<=60">{{text||"无"}}</span>
      </span>
    </a-table>
    <a-divider orientation="left">集群服务</a-divider>
    <a-tree :tree-data="treeData" autoExpandParent showLine />
  </div>
</template>
<script>
import { baseMixin } from '@/store/app-mixin'
import { getClusterInfo, readClusterMessage } from '@/api/index'

const columns = [
  {
    title: '配置名称',
    className: 'mw120',
    dataIndex: 'clusterConfigTitle',
  },
  {
    title: '配置信息',
    className: 'w300',
    dataIndex: 'clusterConfigInfo',
    scopedSlots: { customRender: 'clusterConfigInfo' },
  },
  {
    title: '配置时间',
    className: 'mw120',
    dataIndex: 'clusterConfigCreatetime',
  },
]

export default {
  mixins: [baseMixin],
  props: {
    clusterId: {
      type: Number,
      required: true,
    },
  },
  name: 'detail',
  data() {
    return {
      columns,
      clusterInfo: {},
      deploylist: [],
      listLoading: false,
      treeData: [],
      types: 1,
      unfoldArr: [],
    }
  },
  methods: {
    getClusterInfo() {
      getClusterInfo({ cid: this.clusterId })
        .then(({ status, data, msg }) => {
          if (status === 1) {
            this.clusterInfo = { ...data }
            this.treeData = this.treeParse(data.servicesLists)
          } else {
            this.$message.error(msg)
          }
        })
        .catch((error) => {})
      this.readClusterMessage()
    },
    readClusterMessage() {
      this.listLoading = true
      readClusterMessage({ clusterId: this.clusterId, types: this.types })
        .then(({ status, data, msg }) => {
          if (status === 1) {
            this.deploylist = !Boolean(this.types)
              ? [...this.deploylist, ...data]
              : data || []
            this.listLoading = false
          } else {
            this.listLoading = false
            this.$message.error(msg)
          }
        })
        .catch((error) => {})
    },
    //tree树数据解析
    treeParse(data) {
      return Object.keys(data).map((items) => {
        let obj = {
          title: items,
          children: data[items].map((item) => {
            return { title: item }
          }),
        }
        return obj
      })
    },
    switchTypes() {
      this.types = this.types == 1 ? 0 : 1
      this.readClusterMessage()
    },
    unfold(item) {
      this.unfoldArr = [...this.unfoldArr, ...[item]]
    },
    fold(key) {
      this.unfoldArr.splice(
        this.unfoldArr.findIndex((item) => item == key),
        1
      )
    },
  },
  created() {
    this.getClusterInfo()
  },
}
</script>
<style lang="less" scoped>
.relative {
  position: relative;
}
.deploySwitch {
  display: flex;
  align-items: center;
  position: absolute;
  right: 20px;
  bottom: 0;
  font-size: 14px;
  background: #fff;
  color: #ccc;
  padding: 0 10px;
  z-index: 10;
}
</style>