/** UTC helpers for form input / display. */
export function utcNowIso() {
  return new Date().toISOString()
}

/** Value for <input type="datetime-local"> representing current UTC wall time. */
export function utcNowLocalInput() {
  return new Date().toISOString().slice(0, 16)
}

/** Convert datetime-local value (treated as UTC) to ISO-8601 Instant string. */
export function utcLocalInputToIso(value) {
  if (!value) return utcNowIso()
  // datetime-local has no timezone; we treat it as UTC
  const iso = value.length === 16 ? `${value}:00.000Z` : `${value}Z`
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return utcNowIso()
  return d.toISOString()
}

/** Convert stored Instant/ISO string to datetime-local (UTC components). */
export function isoToUtcLocalInput(iso) {
  if (!iso) return utcNowLocalInput()
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return utcNowLocalInput()
  return d.toISOString().slice(0, 16)
}

/** Display UTC time for tables. */
export function formatUtc(iso) {
  if (!iso) return '-'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return String(iso)
  return d.toISOString().replace('T', ' ').replace(/\.\d{3}Z$/, ' UTC')
}
