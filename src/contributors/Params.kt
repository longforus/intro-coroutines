package contributors

import java.util.prefs.Preferences

private fun prefNode(): Preferences = Preferences.userRoot().node("ContributorsUI")

data class Params(val username: String, val password: String, val org: String, val variant: Variant)

fun loadStoredParams(): Params {
    return prefNode().run {
        Params(
            get("username", "longforus"),
            get("password", "3cbbdc91a07b5d856f3040abc34e5e39f8216262"),
            get("org", "kotlin"),
            Variant.valueOf(get("variant", Variant.BLOCKING.name))
        )
    }
}

fun removeStoredParams() {
    prefNode().removeNode()
}

fun saveParams(params: Params) {
    prefNode().apply {
        put("username", params.username)
        put("password", params.password)
        put("org", params.org)
        put("variant", params.variant.name)
        sync()
    }
}