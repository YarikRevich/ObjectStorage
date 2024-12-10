```plantuml
!pragma teoz true

title

Internal database design of "ObjectStorage"

end title

entity "secret" {
    *id : number <<PK>>
    --
    session : number
    credentials : varchar
}

entity "provider" {
    *id : number <<PK>>
    --
    name : varchar<3>
}

entity "content" {
    *id : number <<PK>>
    *provider : number <<FK>> # provider(id)
    *secret : number <<FK>> # secret(id)
    --
    root : varchar
}

entity "temporate" {
    *id : number <<PK>>
    *provider : number <<FK>> # provider(id)
    *secret : number <<FK>> # secret(id)
    --
    location : varchar
    hash : varchar
    created_at : number
}

content ||...|| secret #magenta : attached to
content }|...|| provider  #magenta : configures
temporate }|...|| secret #magenta : created with
temporate }|...|| provider  #magenta : created for
```