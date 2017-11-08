import lagService from '@/services/LagService'

describe('LagService', () => {
  it('should sum lag', () => {
    expect(lagService.sumLag(null, null)).to.equal(null)
    expect(lagService.sumLag(1, null)).to.equal(1)
    expect(lagService.sumLag(null, 2)).to.equal(2)
    expect(lagService.sumLag(1, 2)).to.equal(3)
  })
  it('should compute total lag', () => {
    expect(lagService.computeTotalLag([{lag: 2}, {}, {lag: -1}, {lag: 3}])).to.equal(5)
    expect(lagService.computeTotalLag([{}, {}])).to.equal(0)
  })
})
