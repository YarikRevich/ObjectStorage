```plantuml
!pragma teoz true

title

Internal database design of "ObjectStorage"

end title

entity "config" {
    *id : number <<PK>>
    *secret: number <<FK>> # secret(id)
    --
    hash : varchar
}

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
    *secret : number <<FK>> # secret(id)
    *provider : number <<FK>> # provider(id)
    --
    root : varchar
}

config ||...|| secret #magenta : attached to
content ||...|| secret #magenta : attached to
content }|...|| provider  #magenta : configures
```