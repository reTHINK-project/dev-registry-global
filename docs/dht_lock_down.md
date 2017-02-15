Global Registry Security Considerations
=======================================

### Introduction

This proposal presents some modifications to the Global Registry/TomP2P, to improve the DHT security.

Right now is possible to any node to enter the DHT, and therefore a malicious node can modify the data stored freely. 
**Is necessary to provide DHT access only to trusted nodes**.

Since it was not possible to implement the previous security extensions at a *Communication* level (since is not possible to intercept every TomP2P messages), this proposal will focus on a security extension at *Storage/Data* level.

### Proposed Solution

#### Storage/Data Level Extension

The proposed solution consists in intercepting every write/read request to store data. The nodes only accept write the received data, if the data contains a signature from a trusted node. The saved data will not keep this information. When the data is read from a node, the node providing the data adds its signature.

#### Message Data Model

The following fields should be added to the message:

 - `nodeCertificate` - the node certificate (PEM encoded);
 - `nodeSignature` - the node digital signature of the dataset;
 - `timestamp`- the message timestamp
 
### Implementation

In order, to validate the dataset the following verifications should be done:

 - the node certificate sent in the message is valid, i.e is signed by a trusted Certificate Authority (CA);
 - the signature added by the node is valid.

This verification should be done when reading or writing from/in the DHT.

### Attack Mitigation

In this section some possible attacks by a malicious node are presented.

#### Create datasets

With this security extension, a malicious node is not capable of creating datasets, since when trying to store the dataset in the DHT the stored certificate will not be signed by a trusted CA.

#### Read datasets

When reading a dataset, the node will verify the signature of the dataset, and if the certificate is signed by a valid CA.

#### Delete datasets

A dataset is considered invalid/deleted if the `revoked` field value is `1`.
Therefore, this is the same case as in a create/modify request, i.e if the node certificate and signature are valid, and `revoked: 1`, we can consider that the entry was "deleted".

#### Hide datasets

A malicious node may try to hide datasets by refusing to return them when queried.
This attack can be mitigated if a node when querying, asks several nodes for the dataset.

#### Replay attack
Since a timestamp is added to the message, the node can check if the message is recent, within a certain time interval.

#### Denial of Service attack
Since is not possible to remove the node from the DHT network, one possible solution is to blacklist a node when it tries to make a invalid write.
