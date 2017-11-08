import lagService from '@/services/JmxService'

describe('JmxService', () => {
  it('should format memory', () => {
    const target = {
      jmx_metrics: {
        'some_heap_memory_stuff': 123.456,
        'some_size': 456.789,
        'some.bytes_stuff': 67.09
      }
    }
    const result = lagService.formatJmxMetrics(target)
    expect(result.length).to.equal(3)
    expect(result[0].value).to.equal('123.456B')
    expect(result[1].value).to.equal('456.789B')
    expect(result[2].value).to.equal('67.090B')
  })
  it('should format number', () => {
    const target = {
      jmx_metrics: {
        'some_number': 123.456789
      }
    }
    const result = lagService.formatJmxMetrics(target)
    expect(result.length).to.equal(1)
    expect(result[0].value).to.equal('123.457')
  })
})
