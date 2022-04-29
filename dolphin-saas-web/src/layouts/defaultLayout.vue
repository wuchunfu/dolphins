<template>
  <a-layout id="default-layout">
    <a-layout-sider
      class="layout-sider"
      v-model="collapsed"
      :trigger="null"
      :width="248"
      collapsible
      :style="`padding-left:${collapsed?0:0}px;`"
    >
      <sidebar :collapsed="collapsed" :sidebarRouters="menus" />
    </a-layout-sider>
    <a-layout class="layout-section">
      <a-layout-header class="layout-section-header">
        <a-icon
          class="trigger"
          :type="collapsed ? 'menu-unfold' : 'menu-fold'"
          @click="() => (collapsed = !collapsed)"
        />
        <right-content :is-mobile="isMobile" />
      </a-layout-header>
      <a-layout-content>
        <router-view class="px-30 pb-30" :key="$route.fullPath" />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>
<script>
import { SIDEBAR_TYPE, TOGGLE_MOBILE_TYPE } from '@/store/mutation-types'
import RightContent from './components/GlobalHeader/RightContent'
import { asyncRouterMap } from '../router'
import Sidebar from './components/Sidebar'

export default {
  components: {
    RightContent,
    Sidebar,
  },
  data() {
    return {
      collapsed: false,
      // 是否手机模式
      isMobile: false,
      menus: [],
    }
  },
  created() {
    let menus = asyncRouterMap.find((item) => item.path === '/').children
    this.menus = menus.filter((item) => !item.hidden)
    // 处理侧栏收起状态
    this.$watch('collapsed', () => {
      this.$store.commit(SIDEBAR_TYPE, this.collapsed)
    })
    this.$watch('isMobile', () => {
      this.$store.commit(TOGGLE_MOBILE_TYPE, this.isMobile)
    })
  },
  mounted() {
    const userAgent = navigator.userAgent
    if (userAgent.indexOf('Edge') > -1) {
      this.$nextTick(() => {
        this.collapsed = !this.collapsed
        setTimeout(() => {
          this.collapsed = !this.collapsed
        }, 16)
      })
    }
  },
  methods: {
    handleCollapse(val) {
      this.collapsed = val
    },
  },
}
</script>
<style lang="less" scoped>
#default-layout {
  min-height: calc(100vh);
  background: #f0f5fc;
  box-sizing: border-box;
  padding: 8px 10px 10px 0;
  .layout-sider {
    background: #e4eaf1;
    padding-right: 20px;
    border-radius: 0px 35px 35px 0px;
    overflow: hidden;
  }
  .layout-section {
    margin-left: 20px;
    background: #ffffff;
    box-shadow: -2px 0px 6px 0px rgba(222, 232, 248, 0.73);
    border-radius: 35px;
    .trigger {
      font-size: 18px;
      line-height: 64px;
      cursor: pointer;
      transition: color 0.3s;
    }
    .trigger:hover {
      color: #1890ff;
    }
    &-header {
      background: transparent;
      padding: 0 36px;
      margin: 10px 0;
    }
  }
}
</style>
