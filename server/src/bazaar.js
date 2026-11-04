/**
 * Checks Cafe Bazaar purchase tokens with the Bazaar developer API ("Pishkhan" API).
 *
 * The API key comes from the Bazaar developer panel (pishkhan.cafebazaar.ir > Developer API) and is
 * sent in the CAFEBAZAAR-PISHKHAN-API-SECRET header. It must stay on the server: never put it in the app.
 */

const DEFAULT_BASE_URL = 'https://pardakht.cafebazaar.ir/devapi/v2'
const REQUEST_TIMEOUT_MS = 10_000

/** Bazaar could not give an answer (network error, timeout, 5xx, bad key). Not the player's fault. */
class BazaarUnavailableError extends Error {
  constructor(message) {
    super(message)
    this.name = 'BazaarUnavailableError'
  }
}

function createBazaarClient({ apiSecret, packageName, baseUrl = DEFAULT_BASE_URL, fetchImpl = fetch }) {
  if (!apiSecret) throw new Error('apiSecret is required')
  if (!packageName) throw new Error('packageName is required')

  async function get(path) {
    let response
    try {
      response = await fetchImpl(baseUrl + path, {
        method: 'GET',
        headers: { 'CAFEBAZAAR-PISHKHAN-API-SECRET': apiSecret, accept: 'application/json' },
        signal: AbortSignal.timeout(REQUEST_TIMEOUT_MS)
      })
    } catch (error) {
      throw new BazaarUnavailableError(`request_failed: ${error.message}`)
    }
    // Bazaar answers 404 for a token it does not know for this package and product.
    if (response.status === 404) return null
    if (!response.ok) throw new BazaarUnavailableError(`bazaar_status_${response.status}`)
    try {
      return await response.json()
    } catch {
      throw new BazaarUnavailableError('bad_json')
    }
  }

  const pkg = encodeURIComponent(packageName)

  /**
   * One in-app product purchase. purchaseState 0 is "purchased" (1 is refunded);
   * consumptionState 0 is "consumed", 1 is "not consumed yet".
   */
  async function verifyProduct(productId, purchaseToken) {
    const body = await get(
      `/api/validate/${pkg}/inapp/${encodeURIComponent(productId)}/purchases/${encodeURIComponent(purchaseToken)}/`
    )
    if (!body) return { valid: false, reason: 'not_found' }
    if (Number(body.purchaseState) !== 0) return { valid: false, reason: 'refunded' }
    return {
      valid: true,
      consumed: Number(body.consumptionState) === 0,
      purchaseTime: Number(body.purchaseTime) || null,
      developerPayload: body.developerPayload ?? null
    }
  }

  /** One subscription. It is active while validUntilTimestampMsec is in the future. */
  async function verifySubscription(subscriptionId, purchaseToken, now = Date.now()) {
    const body = await get(
      `/api/applications/${pkg}/subscriptions/${encodeURIComponent(subscriptionId)}/purchases/${encodeURIComponent(purchaseToken)}/`
    )
    if (!body) return { valid: false, reason: 'not_found' }
    const validUntil = Number(body.validUntilTimestampMsec)
    if (!Number.isFinite(validUntil) || validUntil <= now) return { valid: false, reason: 'expired', validUntil: validUntil || null }
    return { valid: true, validUntil, autoRenewing: Boolean(body.autoRenewing) }
  }

  return { verifyProduct, verifySubscription }
}

module.exports = { createBazaarClient, BazaarUnavailableError }
