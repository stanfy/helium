package com.stanfy.helium.handler.codegen.swift.entity.entities

data class SwiftEntity(val name: String,
                       val properties:List<SwiftProperty> = emptyList()) {
}