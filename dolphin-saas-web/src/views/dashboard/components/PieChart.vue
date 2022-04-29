<template>
  <div :class="className" :style="{height:height,width:width}" />
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
      default: 'chart'
    },
    width: {
      type: String,
      default: '100%'
    },
    height: {
      type: String,
      default: '300px'
    },
    chartData: {
      type: Object/Array,
      required: true
    },
    radius: {
      type: Array,
      default:function(){
        return [40,90]
      }
    },
    title: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      chart: null
    }
  },
  watch: {
    chartData: {
      deep: true,
      handler(val) {
        this.setOptions(val);
      }
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.initChart();
    })
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
      this.setOptions(this.chartData);
    },
    setOptions(chartData = []) {
      this.chart.setOption({
        tooltip: {
          trigger: 'item',
          formatter: '{b} : {c} ({d}%)',
          confirm:true
        },
        legend: {
          left: 'center',
          bottom: '10',
          data: chartData.map(item => item.name)
        },
        series: [
          {
            name: this.title,
            type: 'pie',
            // roseType: 'radius',
            radius: this.radius,
            center: ['50%', '38%'],
            data: chartData,
            animationEasing: 'cubicInOut',
            animationDuration: 2600
          }
        ]
      })

    }
  }
}
</script>
