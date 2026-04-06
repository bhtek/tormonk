mapOf(
    "jupiterVersion" to "5.12.2"
).forEach { (name, version) ->
    project.extra.set(name, version)
}
