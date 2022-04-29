<template>
  <div :class="className" :style="{ height: height, width: width }" />
</template>

<script>
import echarts from 'echarts'
require('echarts/theme/macarons') // echarts theme
import resize from './mixins/resize'

export default {
  mixins: [resize],
  props: {
    className: {
      type: String,
      default: 'chart',
    },
    width: {
      type: String,
      default: '100%',
    },
    height: {
      type: String,
      default: '300px',
    },
    autoResize: {
      type: Boolean,
      default: true,
    },
    chartData: {
      type: Object,
      required: true,
    },
  },
  data() {
    return {
      chart: null,
    }
  },
  watch: {
    chartData: {
      deep: true,
      handler(val) {
        this.setOptions(val)
      },
    },
  },
  mounted() {
    let timer = setTimeout(() => {
      this.initChart()
      clearTimeout(timer)
    }, 20)
  },
  beforeDestroy() {
    if (!this.chart) {
      return
    }
    this.chart.dispose()
    this.chart = null
  },
  methods: {
    initChart() {
      this.chart = echarts.init(this.$el, 'macarons')
      this.setOptions(this.chartData)
    },
    setOptions({ release, rollback, dateTime }) {
      this.chart.setOption({
        xAxis: {
          data: dateTime,
          // boundaryGap: false,
          axisTick: {
            show: false,
          },
          splitLine: {
            //网格线
            lineStyle: {
              type: 'dashed', //设置网格线类型 dotted：虚线   solid:实线
              width: 1,
              //color:"red"
            },
            show: true, //暗藏或显示
          },
          axisLine: {
            show: false,
            lineStyle: {
              color: '#000',
              width: 1, //这里是为了突出显示加上的
            },
          },
        },
        grid: {
          left: 10,
          right: 10,
          bottom: 0,
          top: 40,
          containLabel: true,
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross',
          },
          padding: [5, 10],
        },
        yAxis: {
          type: 'value',
          minInterval: 1,
          axisLabel: {
            formatter: '{value}次',
          },
          axisTick: {
            show: false,
          },
          axisLine: {
            show: false,
            lineStyle: {
              color: '#000',
              width: 1, //这里是为了突出显示加上的
            },
          },
          splitLine: {
            //网格线
            lineStyle: {
              type: 'dashed', //设置网格线类型 dotted：虚线   solid:实线
              width: 1,
              //color:"red"
            },
            show: true, //暗藏或显示
          },
        },
        legend: {
          top: '10',
          right: '40',
          icon: 'circle',
          data: ['发布次数', '回滚次数'],
        },
        series: [
          {
            name: '发布次数',
            symbol: 'none',
            symbolSize: 6,
            itemStyle: {
              normal: {
                color: '#0077FF',
                lineStyle: {
                  color: '#0077FF',
                  width: 2,
                },
              },
            },
            areaStyle: {
              normal: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 1, color: 'rgba(255, 255, 255, .1)' },
                  { offset: 0, color: 'rgba(0, 119, 255, .5)' },
                ]),
              },
            },
            type: 'line',
            smooth: true,
            data: release,
            animationDuration: 2800,
            animationEasing: 'cubicInOut',
          },
          {
            name: '回滚次数',
            type: 'line',
            symbol: 'none',
            symbolSize: 6,
            itemStyle: {
              normal: {
                color: '#00D896',
                lineStyle: {
                  color: '#00D896',
                  width: 2,
                },
              },
            },
            areaStyle: {
              normal: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 1, color: 'rgba(255, 255, 255, .1)' },
                  { offset: 0, color: 'rgba(22, 247, 103, .5)' },
                ]),
              },
            },
            data: rollback,
            smooth: true,
            animationDuration: 2800,
            animationEasing: 'quadraticOut',
          },
        ],
      })
    },
  },
}
</script>
