const normalizeCode = value => String(value || '')
    .trim()
    .replace(/：/g, ':')
    .replace(/[\s\-_]/g, '')

const normalizeName = value => String(value || '').trim()

const splitDetectionNames = value => String(value || '')
    .split('/')
    .map(item => item.trim())
    .filter(Boolean)

export const buildTestingStatusDisplay = (artifact, detections = []) => {
    const artifactCodes = [
        normalizeCode(artifact?.newArtifactCode),
        normalizeCode(artifact?.originalArtifactCode)
    ].filter(Boolean)

    const artifactNames = [
        normalizeName(artifact?.newArtifactName),
        normalizeName(artifact?.originalArtifactName)
    ].filter(Boolean)

    const names = []
    detections.forEach(item => {
        const detectionCode = normalizeCode(item?.artifactCode)
        const detectionName = normalizeName(item?.artifactName)
        const matchedByCode = detectionCode && artifactCodes.includes(detectionCode)
        const matchedByName = !detectionCode && detectionName && artifactNames.includes(detectionName)
        if (!matchedByCode && !matchedByName) return

        const labels = splitDetectionNames(item?.purpose || item?.instrumentName)
        if (labels.length) names.push(...labels)
        else names.push(`检测分析${item?.id || ''}`)
    })

    return [...new Set(names)].join(' / ') || '无'
}

export const decorateArtifactsWithDetections = (artifacts = [], detections = []) => {
    return artifacts.map(item => ({
        ...item,
        testingStatusDisplay: buildTestingStatusDisplay(item, detections)
    }))
}
