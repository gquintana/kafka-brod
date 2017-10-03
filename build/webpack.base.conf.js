var path = require('path')
var loaders = require('./loaders')
var config = require('./config')
var isProduction = process.env.NODE_ENV === 'production'
var sourceDir = path.resolve(__dirname, '../src/main/js')
var testDir = path.resolve(__dirname, '../src/test/js')

function resolve (dir) {
  return path.join(__dirname, '..', dir)
}

module.exports = {
  entry: {
    app: path.join(sourceDir, 'main.js')
  },
  output: {
    path: config.build.targetDir,
    filename: '[name].js',
    publicPath: process.env.NODE_ENV === 'production'
      ? './' // /static/
      : '/'
  },
  resolve: {
    extensions: ['.js', '.vue', '.json'],
    alias: {
      'vue$': 'vue/dist/vue.esm.js',
      '@': sourceDir,
    }
  },
  module: {
    rules: [
      {
        test: /\.(js|vue)$/,
        loader: 'eslint-loader',
        enforce: 'pre',
        include: [sourceDir, testDir],
        options: {
          formatter: require('eslint-friendly-formatter')
        }
      },
      {
        test: /\.vue$/,
        loader: 'vue-loader',
        options: {
          loaders: loaders.cssLoaders({
            sourceMap: isProduction,
            extract: isProduction
          }),
          transformToRequire: {
            source: 'src',
            img: 'src',
            image: 'xlink:href'
          }
        }
      },
      {
        test: /\.js$/,
        loader: 'babel-loader',
        include: [sourceDir, testDir]
      },
      {
        test: /\.(png|jpe?g|gif|svg)(\?.*)?$/,
        loader: 'url-loader',
        options: {
          limit: 10000,
          name: 'img/[name].[hash:7].[ext]'
        }
      },
      {
        test: /\.(woff2?|eot|ttf|otf)(\?.*)?$/,
        loader: 'url-loader',
        options: {
          limit: 10000,
          name: 'fonts/[name].[hash:7].[ext]'
        }
      }
    ]
  }
}
