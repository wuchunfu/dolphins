// 自定义表格工具组件
import RightToolbar from "@/components/RightToolbar"


export default {
  components: {
    RightToolbar
  },
  data() {
    return {
      // 显示搜索条件
      showSearch: false,
    }
  },
  methods: {
    /**
     * 搜索
     */
    search() {
      console.log('searchForm', this.searchForm)
      this.pagination.current = 1
      this.getList()
    },
  }
}
