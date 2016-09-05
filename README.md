# dev-registry-global
Global Registry

## Changelog

### 0.2.0

- added database support for storing all received datasets in a local database (for testing purposes)
- changed ResponseBuilder to reflect the reThink response format

### 0.1.1a

- fixed a bug with the PUT interface

### 0.1.1

- Status window now shows connected nodes
- TomP2P 5.0 beta8
- Updated libraries (Jetty/Netty/etc)
- Code cleaned up
- Additional tests for JWT/Dataset validation

### 0.1.0

- Initial release

## Testbed

- 130.149.22.133:5002
- 130.149.22.134:5002
- 130.149.22.135:5002

## Interfaces

### GET /

Will display version and info about the daemon

    Request:
    GET / HTTP/1.1
    
    Response:
    {"Description":"OK","Code":200, "value":"gReg v0.1.1a#1235 phase1 (2016-01-20) connectedNodes":["130.149.22.135","130.149.22.134"]"}

### GET /guid/{guid}

Gets the dataset (a signed JWT) for the given GUID

#### Example: 
    Request:
    GET /guid/bXBhhJm-o40WBIcXQQECH0-_MqNux6p3ANxt7lFA-Mg HTTP/1.1
    
    Response:
    {"Description":"OK","Code":200, "value":"eyJhbGciOiJFUzI1NiJ9.eyJkYXRhIjoiZXlKbmRXbGtJam9pWWxoQ2FHaEtiUzF2TkRCWFFrbGpXRkZSUlVOSU1DMWZUWEZPZFhnMmNETkJUbmgwTjJ4R1FTMU5aeUlzSW5CMVlteHBZMHRsZVNJNklpMHRMUzB0UWtWSFNVNGdVRlZDVEVsRElFdEZXUzB0TFMwdFRVWlpkMFZCV1VoTGIxcEplbW93UTBGUldVWkxORVZGUVVGdlJGRm5RVVZ1TWpVdlkwNDNTRGwwY0hwRFMwdzJRa3RoVWxWYVozZE1hekpzZFZaVmIzSTJhMWhNWlcxbFMxVlJTM2M1Tm0xSWNuQXlNRmxJUTFCdWFVMVdjRXAxVm1adFdFVkJWMnRFTDBoaFYxZGtLM0l3VVV4UVp6MDlMUzB0TFMxRlRrUWdVRlZDVEVsRElFdEZXUzB0TFMwdElpd2liR0Z6ZEZWd1pHRjBaU0k2SWpJd01UVXRNRGt0TWpSVU1EZzZNalE2TWpjck1EQTZNREFpTENKaFkzUnBkbVVpT2pFc0luVnpaWEpKUkhNaU9sc2ljbVZVU0VsT1N6b3ZMM05sWW1GemRHbGhiaTVuYjJWdVpHOWxjaTV1WlhRdklpd2ljbVZVU0VsT1N6b3ZMMlpoWTJWaWIyOXJMbU52YlM5bWJIVm1abmt4TWpNaVhTd2ljbVYyYjJ0bFpDSTZNQ3dpZEdsdFpXOTFkQ0k2SWpJd01qWXRNRGt0TWpSVU1EZzZNalE2TWpjck1EQTZNREFpTENKellXeDBJam9pVTNCSWRWaDNSVWQzY2s1alJXTkdiMDVUT0V0Mk56bFFlVWRHYkhocE1YWWlmUSJ9.MEQCICV56mMApYLytl4Zn0poYFO9kOHkUBpaoc6VE335v7FaAiBTFf8IKSHWswGoXXmK9CBhQx5tmc6QaGW_mzwzeUdXCA"}

### PUT /guid/{guid}

Writes (creates and updates) a dataset (a signed JWT) for the given GUID

#### Example: 

    Request:
    PUT /guid/bXBhhJm-o40WBIcXQQECH0-_MqNux6p3ANxt7lFA-Mg HTTP/1.1
    
    eyJhbGciOiJFUzI1NiJ9.eyJkYXRhIjoiZXlKbmRXbGtJam9pWWxoQ2FHaEtiUzF2TkRCWFFrbGpXRkZSUlVOSU1DMWZUWEZPZFhnMmNETkJUbmgwTjJ4R1FTMU5aeUlzSW5CMVlteHBZMHRsZVNJNklpMHRMUzB0UWtWSFNVNGdVRlZDVEVsRElFdEZXUzB0TFMwdFRVWlpkMFZCV1VoTGIxcEplbW93UTBGUldVWkxORVZGUVVGdlJGRm5RVVZ1TWpVdlkwNDNTRGwwY0hwRFMwdzJRa3RoVWxWYVozZE1hekpzZFZaVmIzSTJhMWhNWlcxbFMxVlJTM2M1Tm0xSWNuQXlNRmxJUTFCdWFVMVdjRXAxVm1adFdFVkJWMnRFTDBoaFYxZGtLM0l3VVV4UVp6MDlMUzB0TFMxRlRrUWdVRlZDVEVsRElFdEZXUzB0TFMwdElpd2liR0Z6ZEZWd1pHRjBaU0k2SWpJd01UVXRNRGt0TWpSVU1EZzZNalE2TWpjck1EQTZNREFpTENKaFkzUnBkbVVpT2pFc0luVnpaWEpKUkhNaU9sc2ljbVZVU0VsT1N6b3ZMM05sWW1GemRHbGhiaTVuYjJWdVpHOWxjaTV1WlhRdklpd2ljbVZVU0VsT1N6b3ZMMlpoWTJWaWIyOXJMbU52YlM5bWJIVm1abmt4TWpNaVhTd2ljbVYyYjJ0bFpDSTZNQ3dpZEdsdFpXOTFkQ0k2SWpJd01qWXRNRGt0TWpSVU1EZzZNalE2TWpjck1EQTZNREFpTENKellXeDBJam9pVTNCSWRWaDNSVWQzY2s1alJXTkdiMDVUT0V0Mk56bFFlVWRHYkhocE1YWWlmUSJ9.MEQCICV56mMApYLytl4Zn0poYFO9kOHkUBpaoc6VE335v7FaAiBTFf8IKSHWswGoXXmK9CBhQx5tmc6QaGW_mzwzeUdXCA
    
    Response:
    {"Description":"OK","Code":200}

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
				"id": "http://jsonschema.net/rethink/greg/data/salt",
				"type": "string"
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
  "userIDs": ["user://sebastian.goendoer.net/", "user://facebook.com/fluffy123"],
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

# Configuration

    port_server=5002
    network_interface=eth0
    known_hosts=130.149.22.134
    log_path=/usr/local/gReg/logs/
