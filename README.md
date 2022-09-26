# ChorFlow

Information Flow Analysis for Choregraphic Programs

## Instructions

Run the project with the Gradle wrapper in the project root folder:
```shell
$ gradlew run --args "<choreography path> <policy path> <flow mapping path>"
```

A few example choreographies and corresponding policies can be found in `examples`. Flow mappings can be created from `template.map` in `flowmaps/` where a few sample mappings can be found as well.

Example with no flow violations:
```shell
$ gradlew run --args "examples/OpenID/OpenID.chor examples/OpenID/policy2.flow flowmaps/explicit-implicit-flow.map"
```

Same flow violates a stricter policy:
```shell
$ gradlew run --args "examples/OpenID/OpenID.chor examples/OpenID/policy1.flow flowmaps/explicit-implicit-flow.map"
```