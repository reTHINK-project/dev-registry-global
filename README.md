# dev-registry-global
Global Registry

!!! WARNING !!!
default branch is now `tmp_master`

## Changelog

### 0.3.0

- migrated to SpringBoot framework
- 

### 0.2.5

- finalized implementation of changes to dataset and schema
- added "schemaVersion" to dataset
- DatasetTool updated
- Responses now use HTTP response codes

### 0.2.2

- dataset and schema have been updated to contain "userIDs" and "defaults".
- DatasetTool updated

### 0.2.1

- added DatasetTool to test GReg functionality and status (test/DatasetTool.java)
- more detailed implementation of Dataset class
- some small fixes and code additions

### 0.2.0

- changed/updated configuration file handling. see example in this document
- fixed message format

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
    {"Value":"gReg v0.1.1a 1235 (2016-01-20) [ 130.149.22.135 130.149.22.134 ]","Code":200,"Message":"OK"}

### GET /guid/{guid}

Gets the dataset (a signed JWT) for the given GUID

#### Example: 
    Request:
    GET /guid/bXBhhJm-o40WBIcXQQECH0-_MqNux6p3ANxt7lFA-Mg HTTP/1.1
    
    Response:
    {"Message":"OK","Code":200,"Value":"eyJhbGciOiJFUzI1NiJ9.eyJkYXRhIjoiZXlKbmRXbGtJam9pWWxoQ2FHaEtiUzF2TkRCWFFrbGpXRkZSUlVOSU1DMWZUWEZPZFhnMmNETkJUbmgwTjJ4R1FTMU5aeUlzSW5CMVlteHBZMHRsZVNJNklpMHRMUzB0UWtWSFNVNGdVRlZDVEVsRElFdEZXUzB0TFMwdFRVWlpkMFZCV1VoTGIxcEplbW93UTBGUldVWkxORVZGUVVGdlJGRm5RVVZ1TWpVdlkwNDNTRGwwY0hwRFMwdzJRa3RoVWxWYVozZE1hekpzZFZaVmIzSTJhMWhNWlcxbFMxVlJTM2M1Tm0xSWNuQXlNRmxJUTFCdWFVMVdjRXAxVm1adFdFVkJWMnRFTDBoaFYxZGtLM0l3VVV4UVp6MDlMUzB0TFMxRlRrUWdVRlZDVEVsRElFdEZXUzB0TFMwdElpd2liR0Z6ZEZWd1pHRjBaU0k2SWpJd01UVXRNRGt0TWpSVU1EZzZNalE2TWpjck1EQTZNREFpTENKaFkzUnBkbVVpT2pFc0luVnpaWEpKUkhNaU9sc2ljbVZVU0VsT1N6b3ZMM05sWW1GemRHbGhiaTVuYjJWdVpHOWxjaTV1WlhRdklpd2ljbVZVU0VsT1N6b3ZMMlpoWTJWaWIyOXJMbU52YlM5bWJIVm1abmt4TWpNaVhTd2ljbVYyYjJ0bFpDSTZNQ3dpZEdsdFpXOTFkQ0k2SWpJd01qWXRNRGt0TWpSVU1EZzZNalE2TWpjck1EQTZNREFpTENKellXeDBJam9pVTNCSWRWaDNSVWQzY2s1alJXTkdiMDVUT0V0Mk56bFFlVWRHYkhocE1YWWlmUSJ9.MEQCICV56mMApYLytl4Zn0poYFO9kOHkUBpaoc6VE335v7FaAiBTFf8IKSHWswGoXXmK9CBhQx5tmc6QaGW_mzwzeUdXCA","errorCode":0}

### PUT /guid/{guid}

Writes (creates and updates) a dataset (a signed JWT) for the given GUID. PLEASE NOTE THAT THE BODY OF THE HTTP REQUEST IS THE JWT IN PLAIN TEXT, NOT AS ANAMED PARAMETER!

#### Example: 

    Request:
    PUT /guid/bXBhhJm-o40WBIcXQQECH0-_MqNux6p3ANxt7lFA-Mg HTTP/1.1
    
    eyJhbGciOiJFUzI1NiJ9.eyJkYXRhIjoiZXlKbmRXbGtJam9pWWxoQ2FHaEtiUzF2TkRCWFFrbGpXRkZSUlVOSU1DMWZUWEZPZFhnMmNETkJUbmgwTjJ4R1FTMU5aeUlzSW5CMVlteHBZMHRsZVNJNklpMHRMUzB0UWtWSFNVNGdVRlZDVEVsRElFdEZXUzB0TFMwdFRVWlpkMFZCV1VoTGIxcEplbW93UTBGUldVWkxORVZGUVVGdlJGRm5RVVZ1TWpVdlkwNDNTRGwwY0hwRFMwdzJRa3RoVWxWYVozZE1hekpzZFZaVmIzSTJhMWhNWlcxbFMxVlJTM2M1Tm0xSWNuQXlNRmxJUTFCdWFVMVdjRXAxVm1adFdFVkJWMnRFTDBoaFYxZGtLM0l3VVV4UVp6MDlMUzB0TFMxRlRrUWdVRlZDVEVsRElFdEZXUzB0TFMwdElpd2liR0Z6ZEZWd1pHRjBaU0k2SWpJd01UVXRNRGt0TWpSVU1EZzZNalE2TWpjck1EQTZNREFpTENKaFkzUnBkbVVpT2pFc0luVnpaWEpKUkhNaU9sc2ljbVZVU0VsT1N6b3ZMM05sWW1GemRHbGhiaTVuYjJWdVpHOWxjaTV1WlhRdklpd2ljbVZVU0VsT1N6b3ZMMlpoWTJWaWIyOXJMbU52YlM5bWJIVm1abmt4TWpNaVhTd2ljbVYyYjJ0bFpDSTZNQ3dpZEdsdFpXOTFkQ0k2SWpJd01qWXRNRGt0TWpSVU1EZzZNalE2TWpjck1EQTZNREFpTENKellXeDBJam9pVTNCSWRWaDNSVWQzY2s1alJXTkdiMDVUT0V0Mk56bFFlVWRHYkhocE1YWWlmUSJ9.MEQCICV56mMApYLytl4Zn0poYFO9kOHkUBpaoc6VE335v7FaAiBTFf8IKSHWswGoXXmK9CBhQx5tmc6QaGW_mzwzeUdXCA
    
    Response:
    {"Message":"OK","Code":200,"Value":""}

## Dataset

The dataset is expected to be a valid JSON Object of the following form:

### JSON Schema:

```
{
	"$schema": "http://json-schema.org/draft-04/schema#",
	"type": "object",
	"properties": {
		"guid": {
			"type": "string"
		},
		"schemaVersion": {
			"type": "integer"
		},
		"userIDs": {
			"type": "array",
			"items": {
				"type": "object",
				"properties": {
					"uid": {
						"type": "string"
					},
					"domain": {
						"type": "string"
					}
				},
				"required": ["uid", "domain"]
			}
		},
		"lastUpdate": {
			"type": "string"
		},
		"timeout": {
			"type": "string"
		},
		"publicKey": {
			"type": "string"
		},
		"salt": {
			"type": "string"
		},
		"active": {
			"type": "integer"
		},
		"revoked": {
			"type": "integer"
		},
		"defaults": {
			"type": "object",
			"properties": {
				"voice": {
					"type": "string"
				},
				"chat": {
					"type": "string"
				},
				"video": {
					"type": "string"
				}
			},
			"required": ["voice", "chat", "video"]
		},
		"legacy": {
			"type": "array",
			"items": {
				"type": "object",
				"properties": {
					"legacyType": {
						"type": "string"
					},
					"category": {
						"type": "string"
					},
					"description": {
						"type": "string"
					},
					"id": {
						"type": "string"
					}
				},
				"required": ["legacyType", "category", "id"]
			}
		}
	},
	"required": ["guid", "userIDs", "lastUpdate", "timeout", "publicKey", "salt", "active", "revoked", "defaults"]
}
```

### Example:

```
{
  "guid": "iTCLxibssOUXC2BeKctCxDRejbEw2YlvXsJQgdFa06c",
  "schemaVersion": 1,
  "userIDs": [{
    "uid": "user://sebastian.goendoer.net/",
    "domain": "google.com"
  },{
    "uid": "user://facebook.com/fluffy123",
    "domain": "google.com"
  }],
  "lastUpdate":"2015-09-24T08:24:27+00:00",
  "timeout":"2026-09-24T08:24:27+00:00",
  "publicKey":"-----BEGIN PUBLIC KEY-----MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAE0ptQ88nO42/WDfuNNiNrHlaCGTRswXvbvfY9Ttg9RkVfqhBVKK+V1tHkNPp/WRzIQKwLKDgAzujAxzN8LhI7Hg==-----END PUBLIC KEY-----",
  "salt": "SpHuXwEGwrNcEcFoNS8Kv79PyGFlxi1v",
  "active": 1,
  "revoked": 0,
  "defaults": {
    "voice": "a",
    "chat": "b",
    "video": "c"
  },
  "legacy": [{
    "legacyType": "email",
    "category": "work",
    "description": "my primary work email address",
    "id": "fluffy@googlemail.com"
  }]
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

## config

Configuration of the GReg has changed starting with version 0.3.0. No config file is required, but the following parameters can be passed at startup:

-p --port_rest (default "5002")
-n --network_interface (default "eth0")
-n --connect_node (default "130.149.22.133")
-l --log_path (default "logs")

If the parameters are not passed at startup, the instance runs on the default values.

## DatasetTool

### How to run

- In the Eclipse project, simply run test/DatasetTool.java
- Use textual commands to create, edit, upload, resolve, verify, print, datasets. use command "h" for a full list of commands
- Sample is available in the Wiki: https://github.com/reTHINK-project/dev-registry-global/wiki/DatasetTool
