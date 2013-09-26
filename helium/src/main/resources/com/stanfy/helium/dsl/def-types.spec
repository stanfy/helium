package com.stanfy.helium.dsl

import com.stanfy.helium.DefaultType

DefaultType.values().each {
  type it.toString().toLowerCase(Locale.US)
}
