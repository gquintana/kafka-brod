import numeral from 'numeral'

function formatJmxMetric (jmxMetric) {
  const n = jmxMetric.name
  if (n.indexOf('heap_memory') >= 0 ||
        n.endsWith('_size') ||
        n.indexOf('.bytes_') >= 0) {
    // Memory
    jmxMetric.value = numeral(jmxMetric.value).format('0.000b')
  } else if (typeof jmxMetric.value === 'number' && !Number.isInteger(jmxMetric.value)) {
    jmxMetric.value = numeral(jmxMetric.value).format('0.000')
  }
  return jmxMetric
}

function formatJmxMetrics (target) {
  if (!target || !target.jmx_metrics) {
    return null
  }
  const jmxMetrics = target.jmx_metrics
  return Object.keys(jmxMetrics).map(k => {
    return {name: k, value: jmxMetrics[k]}
  }).map(formatJmxMetric)
}

export default {
  formatJmxMetrics: formatJmxMetrics,
  jmxFields: [
    {key: 'name'},
    {key: 'value', tdClass: 'numeric'}
  ]
}
