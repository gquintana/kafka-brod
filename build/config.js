// see http://vuejs-templates.github.io/webpack for documentation.
var path = require('path')
var targetDir = path.resolve(__dirname, '../target/classes/static')

module.exports = {
  build: {
    env: {
      NODE_ENV: '"production"'
    },
    index: path.join(targetDir, 'index.html'),
    targetDir: targetDir,
    resourceDir: path.resolve(__dirname, '../src/main/resources/static'),
    // Run the build command with an extra argument to
    // View the bundle analyzer report after build finishes:
    // `npm run build --report`
    // Set to `true` or `false` to always turn it on or off
    bundleAnalyzerReport: process.env.npm_config_report
  },
  dev: {
    env: {
      NODE_ENV: '"development"'
    },
    port: 8081,
    proxyTable: {
      '/api/**': {
        target: 'http://localhost:8080/kafka'
      }
    }
  }
}
