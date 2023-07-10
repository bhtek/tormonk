mapOf(
    "jupiterVersion" to "5.8.1"
).forEach { (name, version) ->
    project.extra.set(name, version)
}
