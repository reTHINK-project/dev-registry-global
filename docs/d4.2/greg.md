# Identifiers in reTHINK

In reTHINK, users and entities are identified by globally unique, domain-agnostic identifiers called GUID. These globally unique identifiers remain stable and unchanged throughout the entire lifespan of a user account and can be used to address and identify users and devices.


As GUIDs are domain-agnostic, a directory service is used to resolve them to a list of associated user accounts. This discovery service is called Global Registry.
The Global Registry is a distributed, DHT-based directory service that stores and manages identity datasets in the reTHINK ecosystem. By querying the Global Registy, a GUID is resolved to a dataset that comprises the user’s or entity’s current endpoints (UserIDs). A UserID identifies a user account within a CSP’s domain. As each user can own accounts at multiple CSPs, a GUID is typically resolved to a list of UserIDs. Each UserID can then be resolved by the respective CPS’s Domain Registry to the actual running Hyperty instances for this user account, to which a direct connection can be established using the reTHINK environment.

## Dataset

The dataset for each GUID comprises information about the owner of the identifier and his associated user accounts, as well as information required to validate the dataset’s authenticity and integrity.
In general, the dataset is a JSON object with the following (mandatory) properties:

-	“guid:	The globally unique identifier (GUID)
-	“userIDs”: A JSON array with all UserIDs associated with this identifier
-	“lastUpdate”: A XSDDateTime timestamp of the date and time of the last change to this dataset
-	“timeout”: A XSDDateTime timestamp that determines when the information of this dataset becomes outdated
-	“publicKey”: The ECDSA public key of the identity
-	“salt”: The cryptographic salt
-	“active”: Integer flag to determine whether this identity is flagged as inactive (0) or active (1)
-	“revoked”: Integer flag to determine whether this identity has been revoked (1) or not (0)

An example dataset would look like this:

```
{  
   "guid":"iTCLxibssOUXC2BeKctCxDRejbEw2YlvXsJQgdFa06c",
   "userIDs":[  
      "user://sebastian.goendoer.net/",
      "user://facebook.com/fluffy123"
   ],
   "lastUpdate":"2015-09-24T08:24:27+00:00",
   "timeout":"2026-09-24T08:24:27+00:00",
   "publicKey":"-----BEGIN PUBLIC KEY-----MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAE0ptQ88nO42/WDfuNNiNrHlaCGTRswXvbvfY9Ttg9RkVfqhBVKK+V1tHkNPp/WRzIQKwLKDgAzujAxzN8LhI7Hg==-----END PUBLIC KEY-----",
   "salt":"SpHuXwEGwrNcEcFoNS8Kv79PyGFlxi1v",
   "active":1,
   "revoked":0
}
```

The JSON schema for the dataset:

```
{  
   "$schema":"http://json-schema.org/draft-04/schema#",
   "id":"http://jsonschema.net/rethink/greg/data",
   "type":"object",
   "properties":{  
      "guid":{  
         "id":"http://jsonschema.net/rethink/greg/data/guid",
         "type":"string"
      },
      "userIDs":{  
         "id":"http://jsonschema.net/rethink/greg/data/userIDs",
         "type":"array"
      },
      "lastUpdate":{  
         "id":"http://jsonschema.net/rethink/greg/data/lastUpdate",
         "type":"string"
      },
      "timeout":{  
         "id":"http://jsonschema.net/rethink/greg/data/timeout",
         "type":"string"
      },
      "publicKey":{  
         "id":"http://jsonschema.net/rethink/greg/data/publicKey",
         "type":"string"
      },
      "salt":{  
         "id":"http://jsonschema.net/rethink/greg/data/timeout",
         "type":"salt"
      },
      "active":{  
         "id":"http://jsonschema.net/rethink/greg/data/active",
         "type":"integer"
      },
      "revoked":{  
         "id":"http://jsonschema.net/rethink/greg/data/revoked",
         "type":"integer"
      }
   },
   "required":[  
      "guid",
      "userIDs",
      "lastUpdate",
      "timeout",
      "publicKey",
      "salt",
      "active",
      "revoked"
   ]
}
```

## GUID

A GUID is derived from an ECDSA public key and a cryptographic salt by hashing it using the key derivation function PBKDF#2.
First, an ECDSA key pair is generated using the curve “secp256k1”. The public key in PKCS#8-format with all linebreaks removed is then hashed with PBKDF#2 using SHA256 with 10.000 iterations and a cryptographic salt. The output of 256 bit length, encoded using Base64URL, is the GUID.

## Global Registry

The Global Registry is implemented in Java and utilizes TomP2P and Apache Jetty. While TomP2P 4.4 is used to store and manage all datasets in the service across all participating nodes, Jetty implements a REST interface for clients to commit and request datasets. The REST-based interface uses port 5002 and is organized like this:
Method	Request body	Explanation

- HTTP GET /	N/A	Request a status message from the node
- HTTP GET /GUID/:guid	N/A	Retrieves the dataset for the specified GUID.
- HTTP PUT /GUID/:guid	The dataset as a signed JWT	The dataset is stored in the DHT. Older versions will be overwritten.

To prevent manipulation of the dataset by malicious participants, the dataset is stored as a signed JSON Web Token (JWT). The token is signed using the algorithm “ES256”. For compatibility reasons, the dataset is encoded using Base64URL and stored in the JWT as a private claim named “data”. The token is then signed with the private key matching the enclosed public key. This way, the integrity of the dataset can always be verified.
