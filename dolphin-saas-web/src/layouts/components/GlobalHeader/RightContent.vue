<template>
  <div class="right-content">
    <avatar-dropdown :menu="showMenu" :current-user="currentUser" />
    <!-- <select-lang/> -->
  </div>
</template>

<script>
import AvatarDropdown from './AvatarDropdown'
// import SelectLang from '../../components/SelectLang'
import user_avatar from '@/assets/logo.png'
import { mapGetters } from 'vuex'

export default {
  name: 'RightContent',
  components: {
    AvatarDropdown,
    // SelectLang
  },
  props: {
    isMobile: {
      type: Boolean,
      default: () => false,
    },
  },
  data() {
    return {
      showMenu: true,
      currentUser: {},
    }
  },

  computed: {
    ...mapGetters(['avatar', 'nickname']),
  },
  mounted() {
    setTimeout(() => {
      this.currentUser = {
        name: this.nickname,
        avatar: this.avatar || user_avatar,
      }
    }, 500)
  },
}
</script>
<style lang="less" scoped>
.right-content {
  float: right;
  display: flex;
  justify-content: flex-end;
}
</style>