#!/bin/bash
java -jar build/libs/command-line-0.8.2-SNAPSHOT-nodeps.jar \
   -o build/swift-gen-test-out \
   --swift-api-client \
   -Fgenerate-equatables \
   -Fgenerate-random-initializers \
   -HentitiesAccessLevel=public \
   -VprovidedVar=VariableValue \
   src/cmd-test/test.api


# this one should be ran (with 2.3 toolchain)
# TODO: Add ReactiveCocoa module.
# swiftc build/swift-gen-test-out/SwiftAPIClientCore.swift build/swift-gen-test-out/SwiftAPIRequestManager.swift build/swift-gen-test-out/SwiftAPIServiceExample.swift -o SwiftGenTestOut
