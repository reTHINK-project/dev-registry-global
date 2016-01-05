# dev-registry-global
Global Registry

## Interfaces

### GET /guid/{guid}

Gets the dataset (a signed JWT) for the given GUID

### PUT /guid/{guid}

Writes (creates and updates) a dataset (a signed JWT) for the given GUID

## Dataset

The dataset is expected to be a valid JSON Object of the following form:

  {
	  "guid": "EOOVh5C7JHZ51a0AUJQpMlR7cLSkTXDTPXmaLNA/khA=",
	  "userIDs": ["reTHINK://sebastian.goendoer.net/", "reTHINK://facebook.com/fluffy123"],
	  "lastUpdate":"2015-09-24T08:24:27+00:00",
    "timeout":"2016-09-24T08:24:27+00:00",
	  "publicKey":"-----BEGIN PUBLIC KEY-----MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEQpi2tn8QSSyVQEBhy5ocTyltXZpyGN979PgY6wwF0wz8e+RcDRwShpLNHy/qeiLouz2EZZoVXqeyswtw8ReT7g==-----END PUBLIC KEY-----",
	  "salt": "HUCXPszYwKrP7Th99tac9gD5Q5zVP+YKP21+jeDtoyg=",
    "active": 1,
    "revoked": 0
  } 

## GUID

GUIDs can be created with the following algorithm: 

- generate a ECDSA key pair over curve secp256k1
- get the public key in format PKCS#8
- remove all line breaks
- get a string to be used as a salt
- perform PKDF2 with SHA256 with 10000 iterations on the public key, using the salt
- encode the result in Base64url. This is the GUID

## JWT

The Dataset is transferred as a JWT as a claim identified by "data". The JWT MUST be signed using the private key matching the public key in the dataset.
