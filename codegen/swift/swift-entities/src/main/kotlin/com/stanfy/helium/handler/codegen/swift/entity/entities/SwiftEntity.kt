package com.stanfy.helium.handler.codegen.swift.entity.entities

interface SwiftEntity {
  val name: String
  val optional: Boolean
  fun toOptional(): SwiftEntity
}

interface SwiftCpmplexEntity : SwiftEntity {
  val properties: List<SwiftProperty>
}

data class SwiftEntityPrimitive(override val name: String,
                                override val optional: Boolean = false) : SwiftEntity {

  constructor(name: String) : this(name, false)

  override fun toOptional(): SwiftEntity {
    return optional(name)
  }

  companion object {
    fun optional(name: String): SwiftEntityPrimitive {
      return SwiftEntityPrimitive(name, true)
    }
  }
}


data class SwiftEntityStruct(override val name: String,
                             override val properties: List<SwiftProperty> = emptyList(),
                             override val optional: Boolean = false) : SwiftCpmplexEntity {
  constructor(name: String) : this(name, optional = false)

  constructor(name: String, properties: List<SwiftProperty>) : this(name, properties, false)

  override fun toOptional(): SwiftEntity {
    return optional(name, properties)
  }

  companion object {
    fun optional(name: String, properties: List<SwiftProperty>): SwiftEntityStruct {
      return SwiftEntityStruct(name, properties, true)
    }
  }
}


data class SwiftEntityEnumCase(val name: String, val value: String)

data class SwiftEntityEnum(override val name: String,
                           val values: List<SwiftEntityEnumCase>,
                           override val optional: Boolean = false) : SwiftEntity {
  constructor(name: String) : this(name, optional = false, values = emptyList())

  constructor(name: String, values: List<SwiftEntityEnumCase>) : this(name, values, false)

  override fun toOptional(): SwiftEntity {
    return optional(name, values)
  }

  companion object {
    fun optional(name: String, values: List<SwiftEntityEnumCase>): SwiftEntityEnum {
      return SwiftEntityEnum(name, values, true)
    }
  }
}