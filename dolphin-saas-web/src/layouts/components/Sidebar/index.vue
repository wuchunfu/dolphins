<template>
  <div class="sidebar-container">
    <div class="logo" :style="`padding-left:${collapsed?20:0}px;`">
      <logo-svg />
      <h1 v-show="!collapsed">{{ title }}</h1>
    </div>
    <a-menu
      mode="inline"
      :inline-collapsed="collapsed"
      :default-selected-keys="[activeMenu]"
      :default-open-keys="opemMenu"
      :key="activeMenu"
      @select="select"
    >
      <template v-for="item in sidebarRouters">
        <a-menu-item v-if="!item.children" :key="item.path">
          <a-icon v-if="item.meta.icon" :component="item.meta.icon" />
          <a-icon v-else type="star" />
          <span>{{ item.meta.title }}</span>
        </a-menu-item>
        <sub-menu v-else :key="item.path" :menu-info="item" />
      </template>
    </a-menu>
  </div>
</template>
<script>
import defaultSettings from '@/utils/defaultSettings'
import LogoSvg from '@/assets/logo.svg?inline'
import SubMenu from './SubMenu'

export default {
  props: {
    collapsed: {
      type: Boolean,
      required: true,
    },
    sidebarRouters: {
      type: Array,
      required: true,
    },
  },
  components: { LogoSvg, SubMenu },
  computed: {
    activeMenu() {
      const { path } = this.$route
      return path
    },
    opemMenu() {
      const { matched } = this.$route
      if (matched.length > 2) {
        return [matched[matched.length - 2].path]
      }
      return []
    },
  },
  data() {
    return {
      title: defaultSettings.title,
    }
  },
  methods: {
    select({ key }) {
      this.$router.push({ path: key })
    },
  },
}
</script>
<style lang="less" scoped>
@import './sidebar.less';
</style>