package mobi.sevenwinds.app.author

import com.papsign.ktor.openapigen.annotations.type.string.length.MinLength
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import org.joda.time.LocalDateTime

fun NormalOpenAPIRoute.author() {
    route("/author") {
        post<Unit, AuthorResponse, AuthorRequest>(info("Добавить автора")) { _, body ->
            respond(AuthorService.addAuthor(body))
        }
    }
}

data class AuthorRequest(
    @MinLength(5) val fullName: String
)

data class AuthorResponse(
    val id: Int,
    val fullName: String,
    val createdAt: LocalDateTime
)