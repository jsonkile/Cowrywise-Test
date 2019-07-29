package com.golde.cowrywise.Data

import io.realm.RealmObject


open class Rates(
    var key: String? = null,
    var rate: Double? = null
) : RealmObject(){}