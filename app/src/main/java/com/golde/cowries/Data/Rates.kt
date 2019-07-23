package com.golde.cowries.Data

import io.realm.RealmObject


open class Rates(
    var key: String? = null,
    var rate: Double? = null
) : RealmObject(){}