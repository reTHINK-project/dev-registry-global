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
	  "guid": "1V3G0H7EPFG55W52LUG5SN0ZFVTVMQTTZ6Q5TV5BBTIP05IA0E",
	  "UserIDs": ["reTHINK://sebastian.goendoer.net/", "reTHINK://facebook.com/fluffy123"],
	  "lastUpdate":"2015-09-24T08:24:27+00:00",
    "timeout":"2016-09-24T08:24:27+00:00",
	  "publicKey":"-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAriBpYpmZuLuEaW6wipYh7t+QWZjQRqxe952AgRGpnU5pcr2d4IWc76b2tSbB1xnh6VoW2BotWxYgMbVa6we05+LC6cOsHfLLn8wnZTGctqcKGoEDjnhnO0npBcDvr2W9OwtojzcU4GwGWUGTkznbog2zmLsAGS0WhKYG1kC0iZdc6pi8k/kBTmlL5dRVTWM3usZPwK6arVWRHad+AsIoLKX8qiBV0efRgV9BkqkYZqq4XN1rzPN1jwx3nsUzbpymriGw/6yBWcPK8S2V2wDsoDuFV7mPRK9lhseGgGKfvPbw3AvyEhZZYaImPYAlHterOwHXFWPMoQwXpbB0x6Z8vwIDAQAB-----END PUBLIC KEY-----",
	  "salt": "eadf5f8e396724da",
    "active": 1,
    "revoked": 0
  } 

## GUID

The GUID is created by hashing the public key using PBKDF#2 and interpret it as base36. Example:  "1V3G0H7EPFG55W52LUG5SN0ZFVTVMQTTZ6Q5TV5BBTIP05IA0E"

## JWT

The Dataset is transferred as a JWT as a claim identified by "data". The JWT MUST be signed using the private key matching the publich key in the dataset.
