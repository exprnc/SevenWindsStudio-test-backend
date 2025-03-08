package mobi.sevenwinds.app.budget

import io.restassured.RestAssured
import mobi.sevenwinds.app.author.AuthorRequest
import mobi.sevenwinds.app.author.AuthorResponse
import mobi.sevenwinds.app.author.AuthorTable
import mobi.sevenwinds.common.ServerTest
import mobi.sevenwinds.common.jsonBody
import mobi.sevenwinds.common.toResponse
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AuthorApiKtTest : ServerTest() {

    @BeforeEach
    internal fun setUp() {
        transaction { AuthorTable.deleteAll() }
    }

    @Test
    fun testAddAuthor() {
        val request = AuthorRequest("Example Full Name")
        RestAssured.given()
            .jsonBody(request)
            .post("/author")
            .toResponse<AuthorResponse>().let { response ->
                Assert.assertEquals(request.fullName, response.fullName)
            }
    }

    @Test
    fun testInvalidFullName() {
        val invalidRequest = AuthorRequest("exam")
        RestAssured.given()
            .jsonBody(invalidRequest)
            .post("/author")
            .then()
            .statusCode(400)
    }
}