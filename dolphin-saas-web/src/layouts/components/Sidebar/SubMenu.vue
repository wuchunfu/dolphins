<template>
  <a-sub-menu :key="menuInfo.path" v-bind="$props" v-on="$listeners">
    <span slot="title">
      <a-icon v-if="menuInfo.meta.icon" :component="menuInfo.meta.icon" />
      <a-icon v-else type />
      <span>{{ menuInfo.meta.title }}</span>
    </span>
    <template v-for="item in menuInfo.children">
      <a-menu-item v-if="!item.children" :key="item.path">
        <a-icon v-if="item.meta.icon" :component="item.meta.icon" />
        <a-icon v-else />
        <span>{{ item.meta.title }}</span>
      </a-menu-item>
      <sub-menu v-else :key="item.path" :menu-info="item" />
    </template>
  </a-sub-menu>
</template>

<script>
import { Menu } from 'ant-design-vue'
const PandaSvg = {
  template: `
    <svg viewBox="0 0 1024 1024" width="1em" height="1em" fill="currentColor">
    </svg>
  `,
}
export default {
  name: 'SubMenu',
  // must add isSubMenu: true
  isSubMenu: true,
  props: {
    ...Menu.SubMenu.props,
    // Cannot overlap with properties within Menu.SubMenu.props
    menuInfo: {
      type: Object,
      default: () => ({}),
    },
  },
  data() {
    return {
      PandaSvg,
    }
  },
}
</script>
<style lang="less" scoped>
@import './sidebar.less';
</style>
