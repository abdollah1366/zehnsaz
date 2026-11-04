const { test } = require('node:test')
const assert = require('node:assert/strict')
const { createBazaarClient, BazaarUnavailableError } = require('../src/bazaar')

/** A fetch stand-in that records each call and answers with the given status and body. */
function fakeFetch(status, body) {
  const calls = []
  const impl = async (url, options) => {
    calls.push({ url, options })
    if (status instanceof Error) throw status
    return {
      status,
      ok: status >= 200 && status < 300,
      json: async () => body
    }
  }
  return { impl, calls }
}

function client(fetchImpl) {
  return createBazaarClient({ apiSecret: 'secret', packageName: 'com.barati.zehnsaz', fetchImpl, baseUrl: 'https://bazaar.test' })
}

test('a purchased product is valid and the API key goes in the header', async () => {
  const fetch = fakeFetch(200, { purchaseState: 0, consumptionState: 1, purchaseTime: 1700000000000, developerPayload: 'p' })
  const result = await client(fetch.impl).verifyProduct('coins_100', 'tok/1')

  assert.deepEqual(result, { valid: true, consumed: false, purchaseTime: 1700000000000, developerPayload: 'p' })
  assert.equal(fetch.calls[0].url, 'https://bazaar.test/api/validate/com.barati.zehnsaz/inapp/coins_100/purchases/tok%2F1/')
  assert.equal(fetch.calls[0].options.headers['CAFEBAZAAR-PISHKHAN-API-SECRET'], 'secret')
})

test('a refunded product and an unknown token are not valid', async () => {
  assert.deepEqual(await client(fakeFetch(200, { purchaseState: 1 }).impl).verifyProduct('coins_100', 't'), {
    valid: false,
    reason: 'refunded'
  })
  assert.deepEqual(await client(fakeFetch(404, {}).impl).verifyProduct('coins_100', 't'), { valid: false, reason: 'not_found' })
})

test('a subscription is valid only until its end time', async () => {
  const now = 1_000_000
  const active = await client(fakeFetch(200, { validUntilTimestampMsec: now + 1, autoRenewing: true }).impl)
    .verifySubscription('monthly', 't', now)
  assert.deepEqual(active, { valid: true, validUntil: now + 1, autoRenewing: true })

  const expired = await client(fakeFetch(200, { validUntilTimestampMsec: now - 1 }).impl).verifySubscription('monthly', 't', now)
  assert.equal(expired.valid, false)
  assert.equal(expired.reason, 'expired')
})

test('network errors and server errors are reported as Bazaar being unavailable', async () => {
  await assert.rejects(client(fakeFetch(new Error('offline')).impl).verifyProduct('a', 'b'), BazaarUnavailableError)
  await assert.rejects(client(fakeFetch(500, {}).impl).verifyProduct('a', 'b'), BazaarUnavailableError)
  await assert.rejects(client(fakeFetch(401, {}).impl).verifySubscription('a', 'b'), BazaarUnavailableError)
})
