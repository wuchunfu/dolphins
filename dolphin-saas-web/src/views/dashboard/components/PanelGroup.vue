<template>
  <a-row :gutter="5" class="panel-group" type="flex" justify="space-between">
    <a-col :xs="12" :sm="12" :lg="4">
      <div class="card-panel" @click="handleSetLineChartData('newVisitis')">
        <div class="card-panel-description">
          <div class="card-panel-title">活跃服务者数</div>
          <div class="card-panel-time">{{ date[dateType] }}</div>
          <!-- <div class="card-panel-day">今日</div> -->
          <div>
            <count-to
              :start-val="0"
              :end-val="panelData.service_user"
              :duration="2600"
              class="card-panel-num"
            />/人
          </div>
          <!-- <div>
            环比
            <span :class="panelData.commit.ratio > 0 ? 'text-red' : 'text-green'">
              <i :class="panelData.commit.ratio > 0? 'el-icon-caret-top' : 'el-icon-caret-bottom'" />{{ Math.abs(panelData.commit.ratio) }}%
            </span>
            同比
            <span :class="panelData.commit.comparison > 0 ? 'text-red' : 'text-green'">
              <i :class="panelData.commit.comparison > 0 ? 'el-icon-caret-top' : 'el-icon-caret-bottom'" />{{ Math.abs(panelData.commit.comparison) }}%</span>
          </div>-->
        </div>
      </div>
    </a-col>
    <a-col :xs="12" :sm="12" :lg="4">
      <div class="card-panel" @click="handleSetLineChartData('newVisitis')">
        <div class="card-panel-description">
          <div class="card-panel-title">代码提交次数</div>
          <div class="card-panel-time">{{ date[dateType] }}</div>
          <!-- <div class="card-panel-day">今日</div> -->
          <div>
            <count-to
              :start-val="0"
              :end-val="panelData.commits"
              :duration="2600"
              class="card-panel-num"
            />/次
          </div>
          <!-- <div>
            环比
            <span :class="panelData.commit.ratio > 0 ? 'text-red' : 'text-green'">
              <i :class="panelData.commit.ratio > 0? 'el-icon-caret-top' : 'el-icon-caret-bottom'" />{{ Math.abs(panelData.commit.ratio) }}%
            </span>
            同比
            <span :class="panelData.commit.comparison > 0 ? 'text-red' : 'text-green'">
              <i :class="panelData.commit.comparison > 0 ? 'el-icon-caret-top' : 'el-icon-caret-bottom'" />{{ Math.abs(panelData.commit.comparison) }}%</span>
          </div>-->
        </div>
      </div>
    </a-col>
    <a-col :xs="12" :sm="12" :lg="4">
      <div class="card-panel" @click="handleSetLineChartData('messages')">
        <div class="card-panel-description">
          <div class="card-panel-title">上线发布次数</div>
          <div class="card-panel-time">{{ date[dateType] }}</div>
          <!-- <div class="card-panel-day">今日</div> -->
          <div>
            <count-to
              :start-val="0"
              :end-val="panelData.release"
              :duration="2600"
              class="card-panel-num"
            />/次
          </div>
          <!-- <div>
            环比
            <span :class="panelData.release.ratio > 0 ? 'text-red' : 'text-green'">
              <i :class="panelData.release.ratio > 0? 'el-icon-caret-top' : 'el-icon-caret-bottom'" />{{ Math.abs(panelData.release.ratio) }}%
            </span>
            同比
            <span :class="panelData.release.comparison > 0 ? 'text-red' : 'text-green'">
              <i :class="panelData.release.comparison > 0 ? 'el-icon-caret-top' : 'el-icon-caret-bottom'" />{{ Math.abs(panelData.release.comparison) }}%</span>
          </div>-->
        </div>
      </div>
    </a-col>
    <a-col :xs="12" :sm="12" :lg="4">
      <div class="card-panel" @click="handleSetLineChartData('purchases')">
        <div class="card-panel-description">
          <div class="card-panel-title">质量风险数</div>
          <div class="card-panel-time">{{ date[dateType] }}</div>
          <!-- <div class="card-panel-day">今日</div> -->
          <div>
            <count-to
              :start-val="0"
              :end-val="panelData.quality"
              :duration="2600"
              :decimals="0"
              class="card-panel-num"
            />/个
          </div>
          <!-- <div>
            环比
            <span :class="panelData.quality.ratio > 0 ? 'text-red' : 'text-green'">
              <i :class="panelData.quality.ratio > 0? 'el-icon-caret-top' : 'el-icon-caret-bottom'" />{{ Math.abs(panelData.quality.ratio) }}%
            </span>
            同比
            <span :class="panelData.quality.comparison > 0 ? 'text-red' : 'text-green'">
              <i :class="panelData.quality.comparison > 0 ? 'el-icon-caret-top' : 'el-icon-caret-bottom'" />{{ Math.abs(panelData.quality.comparison) }}%</span>
          </div>-->
        </div>
      </div>
    </a-col>
    <a-col :xs="12" :sm="12" :lg="4">
      <div class="card-panel" @click="handleSetLineChartData('shoppings')">
        <div class="card-panel-description">
          <div class="card-panel-title">工程数目总量</div>
          <div class="card-panel-time">{{ date[dateType] }}</div>
          <!-- <div class="card-panel-day">今日</div> -->
          <div>
            <count-to
              :start-val="0"
              :end-val="panelData.projects"
              :duration="2600"
              :decimals="0"
              class="card-panel-num"
            />/次
          </div>
          <!-- <div>
            环比
            <span :class="panelData.efficient.ratio > 0 ? 'text-red' : 'text-green'">
              <i :class="panelData.efficient.ratio > 0? 'el-icon-caret-top' : 'el-icon-caret-bottom'" />{{ Math.abs(panelData.efficient.ratio) }}%
            </span>
            同比
            <span :class="panelData.efficient.comparison > 0 ? 'text-red' : 'text-green'">
              <i :class="panelData.efficient.comparison > 0 ? 'el-icon-caret-top' : 'el-icon-caret-bottom'" />{{ Math.abs(panelData.efficient.comparison) }}%</span>
          </div>-->
        </div>
      </div>
    </a-col>
  </a-row>
</template>

<script>
import CountTo from 'vue-count-to'

export default {
  props: {
    panelData: {
      type: Object,
      required: true,
    },
    dateType: {
      type: Number,
      required: true,
    },
  },
  data() {
    return {
      date: ['', '最近7天', '最近30天', '最近半年', '最近1年', '所有'],
    }
  },
  components: {
    CountTo,
  },
  methods: {
    handleSetLineChartData(type) {
      this.$emit('handleSetLineChartData', type)
    },
  },
}
</script>

<style lang="less" scoped>
.panel-group {
  // margin-top: 18px;
  margin-bottom: 20px;
  .card-panel {
    height: auto;
    cursor: pointer;
    font-size: 12px;
    padding: 20px;
    margin-bottom: 5px;
    overflow: hidden;
    color: #666;
    background: #fff;
    border-radius: 8px;
    border: 1px solid #eee;
    // box-shadow: 4px 4px 40px rgba(0, 0, 0, 0.05);
    &:hover {
      border-radius: 8px;
      box-shadow: 0 0 10px 0 #f0f0f0;
    }
    .card-panel-description {
      .card-panel-title {
        font-size: 16px;
        line-height: 22px;
        font-weight: bold;
      }
      .card-panel-time {
        line-height: 22px;
      }
      .card-panel-day {
        margin-top: 10px;
        line-height: 22px;
      }
      .card-panel-text {
        line-height: 18px;
        color: rgba(0, 0, 0, 0.45);
        font-size: 16px;
        margin-bottom: 12px;
      }

      .card-panel-num {
        font-size: 20px;
      }
    }
  }
}
</style>
