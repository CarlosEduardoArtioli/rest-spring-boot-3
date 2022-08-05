package br.com.caduartioli.restspringboot3.integrationtests.controller.withyaml;

import br.com.caduartioli.restspringboot3.configs.TestConfigs;
import br.com.caduartioli.restspringboot3.integrationtests.controller.withyaml.mapper.YMLMapper;
import br.com.caduartioli.restspringboot3.integrationtests.testcontainers.AbstractIntegrationTest;
import br.com.caduartioli.restspringboot3.integrationtests.vo.BookVO;
import br.com.caduartioli.restspringboot3.integrationtests.vo.security.AccountCredentialsVO;
import br.com.caduartioli.restspringboot3.integrationtests.vo.security.TokenVO;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.EncoderConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(OrderAnnotation.class)
public class BookControllerYamlTest extends AbstractIntegrationTest {

    private static RequestSpecification specification;

    private static YMLMapper objectMapper;

    private static BookVO book;

    @BeforeAll
    public static void setup() {
        objectMapper = new YMLMapper();
        book = new BookVO();
    }

    @Test
    @Order(1)
    public void authorization() {
        AccountCredentialsVO user = new AccountCredentialsVO();
        user.setUsername("leandro");
        user.setPassword("admin123");

        var token =
                given()
                        .config(
                                RestAssuredConfig
                                        .config()
                                        .encoderConfig(EncoderConfig.encoderConfig()
                                                .encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YML, ContentType.TEXT)))
                        .basePath("/auth/signin")
                        .port(TestConfigs.SERVER_PORT)
                        .contentType(TestConfigs.CONTENT_TYPE_YML)
                        .accept(TestConfigs.CONTENT_TYPE_YML)
                        .body(user, objectMapper)
                        .when()
                        .post()
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(TokenVO.class, objectMapper)
                        .getAccessToken();

        specification =
                new RequestSpecBuilder()
                        .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + token)
                        .setBasePath("/api/book/v1")
                        .setPort(TestConfigs.SERVER_PORT)
                        .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                        .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                        .build();
    }

    @Test
    @Order(2)
    public void testCreate() {

        mockBook();

        book = given()
                .config(
                        RestAssuredConfig
                                .config()
                                .encoderConfig(EncoderConfig.encoderConfig()
                                        .encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YML, ContentType.TEXT)))
                .spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_YML)
                .accept(TestConfigs.CONTENT_TYPE_YML)
                .body(book, objectMapper)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(BookVO.class, objectMapper);

        Assertions.assertNotNull(book.getId());
        Assertions.assertNotNull(book.getTitle());
        Assertions.assertNotNull(book.getAuthor());
        Assertions.assertNotNull(book.getPrice());
        Assertions.assertTrue(book.getId() > 0);
        Assertions.assertEquals("Docker Deep Dive", book.getTitle());
        Assertions.assertEquals("Nigel Poulton", book.getAuthor());
        Assertions.assertEquals(55.99, book.getPrice());
    }

    @Test
    @Order(3)
    public void testUpdate() {

        book.setTitle("Docker Deep Dive - Updated");

        BookVO bookUpdated = given()
                .config(
                        RestAssuredConfig
                                .config()
                                .encoderConfig(EncoderConfig.encoderConfig()
                                        .encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YML, ContentType.TEXT)))
                .spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_YML)
                .accept(TestConfigs.CONTENT_TYPE_YML)
                .body(book, objectMapper)
                .when()
                .put()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(BookVO.class, objectMapper);

        Assertions.assertNotNull(bookUpdated.getId());
        Assertions.assertNotNull(bookUpdated.getTitle());
        Assertions.assertNotNull(bookUpdated.getAuthor());
        Assertions.assertNotNull(bookUpdated.getPrice());
        Assertions.assertEquals(bookUpdated.getId(), book.getId());
        Assertions.assertEquals("Docker Deep Dive - Updated", bookUpdated.getTitle());
        Assertions.assertEquals("Nigel Poulton", bookUpdated.getAuthor());
        Assertions.assertEquals(55.99, bookUpdated.getPrice());
    }

    @Test
    @Order(4)
    public void testFindById() {
        var foundBook = given()
                .config(
                        RestAssuredConfig
                                .config()
                                .encoderConfig(EncoderConfig.encoderConfig()
                                        .encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YML, ContentType.TEXT)))
                .spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_YML)
                .accept(TestConfigs.CONTENT_TYPE_YML)
                .pathParam("id", book.getId())
                .when()
                .get("{id}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(BookVO.class, objectMapper);

        Assertions.assertNotNull(foundBook.getId());
        Assertions.assertNotNull(foundBook.getTitle());
        Assertions.assertNotNull(foundBook.getAuthor());
        Assertions.assertNotNull(foundBook.getPrice());
        Assertions.assertEquals(foundBook.getId(), book.getId());
        Assertions.assertEquals("Docker Deep Dive - Updated", foundBook.getTitle());
        Assertions.assertEquals("Nigel Poulton", foundBook.getAuthor());
        Assertions.assertEquals(55.99, foundBook.getPrice());
    }

    @Test
    @Order(5)
    public void testDelete() {
        given()
                .config(
                        RestAssuredConfig
                                .config()
                                .encoderConfig(EncoderConfig.encoderConfig()
                                        .encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YML, ContentType.TEXT)))
                .spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_YML)
                .accept(TestConfigs.CONTENT_TYPE_YML)
                .pathParam("id", book.getId())
                .when()
                .delete("{id}")
                .then()
                .statusCode(204);
    }

    @Test
    @Order(6)
    public void testFindAll() {
        var response = given()
                .config(
                        RestAssuredConfig
                                .config()
                                .encoderConfig(EncoderConfig.encoderConfig()
                                        .encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YML, ContentType.TEXT)))
                .spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_YML)
                .accept(TestConfigs.CONTENT_TYPE_YML)
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(BookVO[].class, objectMapper);


        List<BookVO> content = Arrays.asList(response);

        BookVO foundBookOne = content.get(0);

        Assertions.assertNotNull(foundBookOne.getId());
        Assertions.assertNotNull(foundBookOne.getTitle());
        Assertions.assertNotNull(foundBookOne.getAuthor());
        Assertions.assertNotNull(foundBookOne.getPrice());
        Assertions.assertTrue(foundBookOne.getId() > 0);
        Assertions.assertEquals("Working effectively with legacy code", foundBookOne.getTitle());
        Assertions.assertEquals("Michael C. Feathers", foundBookOne.getAuthor());
        Assertions.assertEquals(49.00, foundBookOne.getPrice());

        BookVO foundBookFive = content.get(4);

        Assertions.assertNotNull(foundBookFive.getId());
        Assertions.assertNotNull(foundBookFive.getTitle());
        Assertions.assertNotNull(foundBookFive.getAuthor());
        Assertions.assertNotNull(foundBookFive.getPrice());
        Assertions.assertTrue(foundBookFive.getId() > 0);
        Assertions.assertEquals("Code complete", foundBookFive.getTitle());
        Assertions.assertEquals("Steve McConnell", foundBookFive.getAuthor());
        Assertions.assertEquals(58.0, foundBookFive.getPrice());
    }

    private void mockBook() {
        book.setTitle("Docker Deep Dive");
        book.setAuthor("Nigel Poulton");
        book.setPrice(55.99);
        book.setLaunchDate(new Date());
    }
}