```plantuml
title

High-level design of "ObjectStorage"

end title

cloud "External environment" {
actor "Client"
actor "Diagnostics"
}

component "Control plane" {
node "API Server"
node "Backup manager"
node "Temporary storage"
node "Metrics registry"
}

cloud " Storage vendors" {
entity "S3"
entity "Google Cloud Service"
}

[Client] <--> [API Server]: " Send requests"
[API Server] --> [Temporary storage]: " Schedule storage modification operation"
[Temporary storage] <--> [ Storage vendors]: " Execute scheduled storage modification operation"
[Backup manager] <--> [ Storage vendors]: " Perform backup operations"
[API Server] <--> [Metrics registry]: " Retrieve metrics"
[Diagnostics] <--> [Metrics registry]: " Scrap metrics"
```