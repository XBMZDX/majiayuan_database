const DAY_MS = 86400000

const parseDate = (dateStr) => {
    if (!dateStr) return null
    const text = String(dateStr).slice(0, 10)
    const parts = text.split('-').map(Number)
    if (parts.length !== 3 || parts.some(Number.isNaN)) return null
    const date = new Date(parts[0], parts[1] - 1, parts[2])
    return Number.isNaN(date.getTime()) ? null : date
}

export const getRawTimePercent = (dateStr, timeRange) => {
    if (!dateStr || !timeRange?.days) return 0
    const date = parseDate(dateStr)
    const min = parseDate(timeRange.min)
    if (!date || !min) return 0
    return Math.max(0, Math.min(100, ((date - min) / DAY_MS) / timeRange.days * 100))
}

export const buildSpacedTimelineLayout = (items, timeRange, options = {}) => {
    const minGapPercent = options.minGapPercent ?? 8
    const edgePaddingPercent = options.edgePaddingPercent ?? 3
    const list = (items || []).map((item, index) => ({
        index,
        rawPct: getRawTimePercent(item.date, timeRange)
    }))
    const count = list.length
    if (!count) return []

    const leftEdge = edgePaddingPercent
    const rightEdge = 100 - edgePaddingPercent
    const available = Math.max(rightEdge - leftEdge, 1)
    const gap = count > 1 ? Math.min(minGapPercent, available / (count - 1)) : 0
    const positions = list.map(item => Math.max(leftEdge, Math.min(rightEdge, item.rawPct)))

    let clusterStart = 0
    for (let i = 1; i < positions.length; i += 1) {
        if (list[i].rawPct - list[i - 1].rawPct >= gap) clusterStart = i
        if (positions[i] < positions[i - 1] + gap) {
            positions[i] = positions[i - 1] + gap
            if (positions[i] > rightEdge) {
                const overflow = positions[i] - rightEdge
                for (let j = clusterStart; j <= i; j += 1) positions[j] -= overflow
            }
        }
    }

    if (positions[0] < leftEdge) {
        const underflow = leftEdge - positions[0]
        for (let i = 0; i < positions.length; i += 1) positions[i] += underflow
    }

    return positions.map((pct, index) => ({
        pct: Math.max(0, Math.min(100, pct)),
        rawPct: list[index].rawPct
    }))
}
