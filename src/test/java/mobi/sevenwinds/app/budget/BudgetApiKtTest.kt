package mobi.sevenwinds.app.budget

import io.restassured.RestAssured
import mobi.sevenwinds.app.author.AuthorRequest
import mobi.sevenwinds.app.author.AuthorResponse
import mobi.sevenwinds.common.ServerTest
import mobi.sevenwinds.common.jsonBody
import mobi.sevenwinds.common.toResponse
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BudgetApiKtTest : ServerTest() {

    @BeforeEach
    internal fun setUp() {
        transaction { BudgetTable.deleteAll() }
    }

    @Test
    fun testBudgetPagination() {
        val authorResponse = addAuthor(AuthorRequest("Alfir Rakhimov"))
        addBudget(BudgetRequest(2020, 5, 10, BudgetType.Приход, authorResponse.id))
        addBudget(BudgetRequest(2020, 5, 5, BudgetType.Приход, authorResponse.id))
        addBudget(BudgetRequest(2020, 5, 20, BudgetType.Приход, authorResponse.id))
        addBudget(BudgetRequest(2020, 5, 30, BudgetType.Приход, authorResponse.id))
        addBudget(BudgetRequest(2020, 5, 40, BudgetType.Приход, authorResponse.id))
        addBudget(BudgetRequest(2030, 1, 1, BudgetType.Расход, authorResponse.id))

        RestAssured.given()
            .queryParam("authorFullName", "Rakhimov")
            .queryParam("limit", 3)
            .queryParam("offset", 1)
            .get("/budget/year/2020/stats")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println("${response.total} / ${response.items} / ${response.totalByType}")

                Assert.assertEquals(3, response.total)
                Assert.assertEquals(3, response.items.size)
                Assert.assertEquals(60, response.totalByType[BudgetType.Приход.name])
            }
    }

    @Test
    fun testStatsSortOrder() {
        addBudget(BudgetRequest(2020, 5, 100, BudgetType.Приход, null))
        addBudget(BudgetRequest(2020, 1, 5, BudgetType.Приход, null))
        addBudget(BudgetRequest(2020, 5, 50, BudgetType.Приход, null))
        addBudget(BudgetRequest(2020, 1, 30, BudgetType.Приход, null))
        addBudget(BudgetRequest(2020, 5, 400, BudgetType.Приход, null))

        // expected sort order - month ascending, amount descending

        RestAssured.given()
            .get("/budget/year/2020/stats?limit=100&offset=0")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println(response.items)

                Assert.assertEquals(400, response.items[0].amount)
                Assert.assertEquals(100, response.items[1].amount)
                Assert.assertEquals(50, response.items[2].amount)
                Assert.assertEquals(30, response.items[3].amount)
                Assert.assertEquals(5, response.items[4].amount)
            }
    }

    @Test
    fun testInvalidMonthValues() {
        RestAssured.given()
            .jsonBody(BudgetRequest(2020, -5, 5, BudgetType.Приход, null))
            .post("/budget/add")
            .then().statusCode(400)

        RestAssured.given()
            .jsonBody(BudgetRequest(2020, 15, 5, BudgetType.Приход, null))
            .post("/budget/add")
            .then().statusCode(400)
    }

    private fun addAuthor(request: AuthorRequest): AuthorResponse {
        return RestAssured.given()
            .jsonBody(request)
            .post("/author")
            .toResponse<AuthorResponse>().let { response ->
                Assert.assertEquals(request.fullName, response.fullName)
                response
            }
    }

    private fun addBudget(request: BudgetRequest) {
        RestAssured.given()
            .jsonBody(request)
            .post("/budget/add")
            .toResponse<BudgetResponse>().let { response ->
                Assert.assertEquals(request.year, response.year)
                Assert.assertEquals(request.month, response.month)
                Assert.assertEquals(request.amount, response.amount)
                Assert.assertEquals(request.type, response.type)
            }
    }
}