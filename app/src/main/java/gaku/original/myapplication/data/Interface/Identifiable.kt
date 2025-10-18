package gaku.original.myapplication.data.Interface

interface HasId {
    var id: String?
}

interface CommonProperty : HasId {
    var timestamp: Long?
}