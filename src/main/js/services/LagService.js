function isValidLag (lag) {
  return lag === 0 || lag > 0
}
function sumLag (lag1, lag2) {
  if (isValidLag(lag1)) {
    if (isValidLag(lag2)) {
      return lag1 + lag2
    } else {
      return lag1
    }
  } else {
    return lag2
  }
}

export default {
  sumLag: sumLag,
  computeTotalLag: function (partitions) {
    return partitions.reduce(sumLag, 0)
  }
}
