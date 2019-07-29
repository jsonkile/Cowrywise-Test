package com.golde.cowrywise.Data

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required


open class Bits(
    @PrimaryKey @Required var id: String? = null,
    var apiKey: String? = null
) : RealmObject(){}