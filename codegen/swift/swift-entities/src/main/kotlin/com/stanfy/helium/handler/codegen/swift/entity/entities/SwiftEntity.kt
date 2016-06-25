package com.stanfy.helium.handler.codegen.swift.entity.entities

interface SwiftEntity {
  val name: String
}

interface SwiftCpmplexEntity : SwiftEntity {
  val properties: List<SwiftProperty>
}

data class SwiftEntityPrimitive(override val name: String) : SwiftEntity


data class SwiftEntityStruct(override val name: String,
                             override val properties: List<SwiftProperty> = emptyList()) : SwiftCpmplexEntity

data class SwiftEntityEnumCase(val name: String, val value:String)
data class SwiftEntityEnum(override val name: String,
                           val values: List<SwiftEntityEnumCase>) : SwiftEntity