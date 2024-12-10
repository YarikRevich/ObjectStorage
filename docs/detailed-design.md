```plantuml
!pragma teoz true

title 
    Detailed design of "ObjectStorage" 
end title

box "External environment" #MOTIVATION
actor "Client" as client
actor "Diagnostics" as diagnostics
end box

box "Control plain" #MOTIVATION
participant "API Server" as apiserver
entity "Metrics registry" as metricsregistry
participant "Backup" as backup
participant "Temporate storage" as temporatestorage
database "Local storage" as localstorage

box "Third-party resource" #MOTIVATION
entity "Provider service" as externalservice 
end box

activate apiserver

loop diagnostics retrieval

diagnostics -> apiserver : request metrics scraping
apiserver --> diagnostics : srapped metrics

activate metricsregistry
apiserver -> metricsregistry: request metrics scraping
metricsregistry --> apiserver : scrapped metrics
deactivate metricsregistry

end loop

loop backup mechanism

activate backup
activate externalservice
apiserver -> backup : request data backup
backup -> externalservice : request data backup
externalservice --> backup : retrieved data
backup --> apiserver : retrieved data
deactivate backup
deactivate externalservice

end loop

loop temporate storage mechanism

activate externalservice
temporatestorage ->> externalservice : request object upload
deactivate externalservice

end loop

group /v1/content POST

client -> apiserver: retrieve available content

opt if any object is still not processed

activate temporatestorage
apiserver -> temporatestorage: request all the available objects details
temporatestorage --> apiserver : retrieved objects details
deactivate temporatestorage

end opt

activate externalservice
apiserver -> externalservice: request all the available objects details
externalservice --> apiserver : retrieved objects details
deactivate externalservice

apiserver --> client: retrieved content

end group

group /v1/content/apply POST

client -> apiserver: apply content configuration

activate localstorage
apiserver -> localstorage: apply provided user configuration
localstorage -> apiserver: user configuration update result
deactivate localstorage

end group

group /v1/content/withdraw DELETE

client -> apiserver: withdraw configuration for the user

activate localstorage
apiserver -> localstorage: remove configuration for the given user
localstorage -> apiserver: user configuration removal result
deactivate localstorage

end group

group /v1/content/object/upload POST

client -> apiserver: upload content object

activate temporatestorage
apiserver ->> temporatestorage: schedule object upload
deactivate temporatestorage

end group

group /v1/content/object/download POST

client -> apiserver: download selected content object

alt if the content object is still not processed

activate temporatestorage
apiserver -> temporatestorage: request content object
temporatestorage --> apiserver : retrieved content object
deactivate temporatestorage

else

activate externalservice
apiserver -> externalservice: request content object
externalservice --> apiserver : retrieved content object
deactivate externalservice

end

apiserver --> client: downloaded content object

end group

group /v1/content/backup/download POST

client -> apiserver: download selected content backup

apiserver --> client: downloaded content backup

end group

group /v1/content/object/clean DELETE

client -> apiserver: clean selected content object

opt if any object is still not processed

activate temporatestorage
apiserver -> temporatestorage: remove requested object for the given user
temporatestorage -> apiserver: object removal result
deactivate temporatestorage

end opt

activate externalservice
apiserver -> externalservice: remove requested object for the given user
externalservice -> apiserver: object removal result
deactivate externalservice

end group

group /v1/content/clean/all DELETE

client -> apiserver: clean all the content for the given user

opt if any object is still not processed

activate temporatestorage
apiserver -> temporatestorage: remove all content for the given user
temporatestorage -> apiserver: content removal result
deactivate temporatestorage

end opt

activate externalservice
apiserver -> externalservice: remove all content for the given user
externalservice -> apiserver: content removal result
deactivate externalservice

end group

end box

group /v1/secrets/acquire POST

client -> apiserver: create jwt token

activate externalservice
apiserver -> externalservice: verify given secrets
externalservice -> apiserver: result of verification
deactivate externalservice

apiserver --> client: created jwt token

end group

group /v1/health GET
client -> apiserver: retrieve health info
apiserver --> client: retrieved health info
end group

group /v1/info/version GET
client -> apiserver: retrieve version info
apiserver --> client: retrieved version info
end group

deactivate apiserver
```