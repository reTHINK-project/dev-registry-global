# dev-registry-global
Global Registry

## Interfaces

### GET /

Will display version and info about the daemon

### GET /guid/{guid}

Gets the dataset (a signed JWT) for the given GUID

### PUT /guid/{guid}

Writes (creates and updates) a dataset (a signed JWT) for the given GUID

## Dataset

The dataset is expected to be a valid JSON Object of the following form:

### JSON Schema:

```
{
		"$schema": "http://json-schema.org/draft-04/schema#",
		"id": "http://jsonschema.net/rethink/greg/data",
		"type": "object",
		"properties": {
			"guid": {
				"id": "http://jsonschema.net/rethink/greg/data/guid",
				"type": "string"
			},
			"userIDs": {
				"id": "http://jsonschema.net/rethink/greg/data/userIDs",
				"type": "array"
			},
			"lastUpdate": {
				"id": "http://jsonschema.net/rethink/greg/data/lastUpdate",
				"type": "string"
			},
			"timeout": {
				"id": "http://jsonschema.net/rethink/greg/data/timeout",
				"type": "string"
			},
			"publicKey": {
				"id": "http://jsonschema.net/rethink/greg/data/publicKey",
				"type": "string"
			},
			"salt": {
				"id": "http://jsonschema.net/rethink/greg/data/timeout",
				"type": "salt"
			},
			"active": {
				"id": "http://jsonschema.net/rethink/greg/data/active",
				"type": "integer"
			},
			"revoked": {
				"id": "http://jsonschema.net/rethink/greg/data/revoked",
				"type": "integer"
			}
		},
		"required": ["guid", "userIDs", "lastUpdate", "timeout", "publicKey", "salt", "active", "revoked"]
	}
```

### Example:

```
{
  "guid": "iTCLxibssOUXC2BeKctCxDRejbEw2YlvXsJQgdFa06c",
  "userIDs": ["reTHINK://sebastian.goendoer.net/", "reTHINK://facebook.com/fluffy123"],
  "lastUpdate":"2015-09-24T08:24:27+00:00",
  "timeout":"2026-09-24T08:24:27+00:00",
  "publicKey":"-----BEGIN PUBLIC KEY-----MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAE0ptQ88nO42/WDfuNNiNrHlaCGTRswXvbvfY9Ttg9RkVfqhBVKK+V1tHkNPp/WRzIQKwLKDgAzujAxzN8LhI7Hg==-----END PUBLIC KEY-----",
  "salt": "SpHuXwEGwrNcEcFoNS8Kv79PyGFlxi1v",
  "active": 1,
  "revoked": 0
}
```

The Dataset is transferred as a JWT as a claim identified by "data". The JWT MUST be signed using the private key matching the public key in the dataset.

### create JWT

- Create the dataset as a JSONObject as described above
- serialize the JSONObject
- encode it via Base64URL
- create a JWT
- set a claim "data" with the Base64URL-encoded JSONObject as value
- sign the JWT using the private key matching the enclosed public key
- the result can be sent to the gReg via PUT

see also: https://github.com/reTHINK-project/dev-registry-global/wiki/Test-dataset-jwt-creation-verification

## GUID

GUIDs can be created with the following algorithm: 

- generate a ECDSA key pair over curve secp256k1
- get the public key in format PKCS#8
- remove all line breaks
- get a string to be used as a salt
- perform PKDF2 with SHA256 with 10000 iterations on the public key, using the salt
- encode the result in Base64url. This is the GUID
