const path = require("path");
const TerserPlugin = require("terser-webpack-plugin");
const CompressionPlugin = require('compression-webpack-plugin');

const name = process.env.VUE_APP_TITLE || "元豚科技";

const port = process.env.port || process.env.npm_config_port || 80;

function resolve(dir) {
  return path.join(__dirname, dir);
}
//vue.config.js  https://cli.vuejs.org/zh/config/#css-loaderoptions
module.exports = {
  publicPath: process.env.NODE_ENV === "production" ? "/" : "/",
  lintOnSave: false,
  outputDir: 'dist',
  productionSourceMap: false,
  assetsDir: 'static',
  // webpack-dev-server config
  devServer: {
    host: "0.0.0.0",
    port: port,
    open: true,
    proxy: {
      [process.env.VUE_APP_BASE_API]: {
        target: `http://81.70.93.104:8091/`,
        changeOrigin: true,
        ws: false,
        pathRewrite: {
          ["^" + process.env.VUE_APP_BASE_API]: ""
        }
      }
    },
    disableHostCheck: true,
  },
  css: {
    loaderOptions: {
      less: {
        modifyVars: {
          'border-radius-base': '6px',
          'card-radius': '10px'
        },
        // DO NOT REMOVE THIS LINE
        javascriptEnabled: true
      }
    }
  },
  configureWebpack: {
    name: name,
    resolve: {
      alias: {
        "@$": resolve("src"),
      },
    },
    optimization: {
      //Remove the console
      minimizer: [
        new TerserPlugin({
          cache: true,
          sourceMap: false,
          parallel: true,
          terserOptions: {
            ecma: undefined,
            warnings: false,
            parse: {},
            compress: {
              drop_console: true,
              drop_debugger: true,
              pure_funcs: ["console.log"],
            },
          },
        }),
      ],
    },
    plugins: [
      new CompressionPlugin({
        filename: "[path].gz[query]",
        //压缩算法
        algorithm: "gzip",
        //匹配文件
        test: /\.js$|\.css$|\.html$/,
        //压缩超过此大小的文件,以字节为单位
        threshold: 10240,
        minRatio: 0.8,
      })
    ]
  },
  chainWebpack(config) {
    config.plugins.delete("preload"); // TODO: need test
    config.plugins.delete("prefetch"); // TODO: need test

    const svgRule = config.module.rule('svg');

    svgRule.uses.clear();

    svgRule
      .use('babel-loader')
      .loader('babel-loader')
      .end()
      .use('vue-svg-loader')
      .loader('vue-svg-loader');

    config.when(process.env.NODE_ENV !== "development", (config) => {
      config
        .plugin("ScriptExtHtmlWebpackPlugin")
        .after("html")
        .use("script-ext-html-webpack-plugin", [
          {
            // `runtime` must same as runtimeChunk name. default is `runtime`
            inline: /runtime\..*\.js$/,
          },
        ])
        .end();
      config.optimization.splitChunks({
        chunks: "all",
        cacheGroups: {
          libs: {
            name: "chunk-libs",
            test: /[\\/]node_modules[\\/]/,
            priority: 10,
            chunks: "initial", // only package third parties that are initially dependent
          },
          antd: {
            name: 'chunk-antd', // split elementUI into a single package
            priority: 20, // the weight needs to be larger than libs and app or it will be packaged into libs or app
            test: /[\\/]node_modules[\\/]_?ant-design-vue(.*)/ // in order to adapt to cnpm
          },
          commons: {
            name: "chunk-commons",
            test: resolve("src/components"), // can customize your rules
            minChunks: 3, //  minimum common number
            priority: 5,
            reuseExistingChunk: true,
          },
        },
      });
    });
  },
};
