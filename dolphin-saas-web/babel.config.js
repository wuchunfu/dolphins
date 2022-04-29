module.exports = {
  presets: [
    '@vue/cli-plugin-babel/preset'
  ],
  //babel-plugin-import
  plugins: [
    [
      "import",
      { libraryName: "ant-design-vue", libraryDirectory: "es", style: true }
    ]
  ]
}
