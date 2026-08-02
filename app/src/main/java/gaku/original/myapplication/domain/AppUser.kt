package gaku.original.myapplication.domain

import gaku.original.myapplication.data.Interface.HasId

data class AppUser(
    override var id: String?,
    val email:String? = null,
    val name:String? = null
): HasId