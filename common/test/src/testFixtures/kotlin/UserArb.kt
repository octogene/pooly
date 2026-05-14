import dev.octogene.pooly.core.PasswordHash
import dev.octogene.pooly.core.User
import dev.octogene.pooly.core.UserRole
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.email
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string

/**
 * Property generator for [dev.octogene.pooly.core.PasswordHash].
 */
fun Arb.Companion.passwordHash(): Arb<PasswordHash> = Arb.string(minSize = 10).map { PasswordHash(it) }

/**
 * Property generator for [dev.octogene.pooly.core.User].
 */
fun Arb.Companion.user(): Arb<User> = arbitrary {
    User(
        username = Arb.string(minSize = 3, maxSize = 20).bind(),
        email = Arb.email().orNull().bind(),
        passwordHash = Arb.passwordHash().bind(),
        role = Arb.enum<UserRole>().bind(),
    )
}

val userArb = Arb.user()
