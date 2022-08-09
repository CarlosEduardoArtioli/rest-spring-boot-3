package br.com.caduartioli.restspringboot3.integrationtests.controller.withxml;

import br.com.caduartioli.restspringboot3.configs.TestConfigs;
import br.com.caduartioli.restspringboot3.integrationtests.testcontainers.AbstractIntegrationTest;
import br.com.caduartioli.restspringboot3.integrationtests.vo.BookVO;
import br.com.caduartioli.restspringboot3.integrationtests.vo.pagemodels.PagedModelBook;
import br.com.caduartioli.restspringboot3.integrationtests.vo.security.AccountCredentialsVO;
import br.com.caduartioli.restspringboot3.integrationtests.vo.security.TokenVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(OrderAnnotation.class)
public class BookControllerXmlTest extends AbstractIntegrationTest {

    private static RequestSpecification specification;
    private static XmlMapper objectMapper;

    private static BookVO book;

    @BeforeAll
    public static void setup() {
        objectMapper = new XmlMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        book = new BookVO();
    }

    @Test
    @Order(0)
    public void authorization() {

        AccountCredentialsVO user = new AccountCredentialsVO("leandro", "admin123");

        var accessToken = given()
                .basePath("/auth/signin")
                .port(TestConfigs.SERVER_PORT)
                .contentType(TestConfigs.CONTENT_TYPE_XML)
                .accept(TestConfigs.CONTENT_TYPE_XML)
                .body(user)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(TokenVO.class)
                .getAccessToken();

        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + accessToken)
                .setBasePath("/api/book/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
    }

    @Test
    @Order(1)
    public void testCreate() throws JsonProcessingException {
        mockBook();

        var content = given().spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_XML)
                .accept(TestConfigs.CONTENT_TYPE_XML)
                .body(book)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        book = objectMapper.readValue(content, BookVO.class);

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
    @Order(2)
    public void testUpdate() throws JsonProcessingException {

        book.setTitle("Docker Deep Dive - Updated");

        var content = given().spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_XML)
                .accept(TestConfigs.CONTENT_TYPE_XML)
                .body(book)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        BookVO bookUpdated = objectMapper.readValue(content, BookVO.class);

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
    @Order(3)
    public void testFindById() throws JsonProcessingException {
        mockBook();

        var content = given().spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_XML)
                .accept(TestConfigs.CONTENT_TYPE_XML)
                .pathParam("id", book.getId())
                .when()
                .get("{id}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        BookVO foundBook = objectMapper.readValue(content, BookVO.class);

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
    @Order(4)
    public void testDelete() {

        given().spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_XML)
                .accept(TestConfigs.CONTENT_TYPE_XML)
                .pathParam("id", book.getId())
                .when()
                .delete("{id}")
                .then()
                .statusCode(204);
    }

    @Test
    @Order(5)
    public void testFindAll() throws JsonProcessingException {

        var content = given().spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_XML)
                .accept(TestConfigs.CONTENT_TYPE_XML)
                .queryParams("page", 0 , "limit", 12, "direction", "asc")
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        PagedModelBook wrapper = objectMapper.readValue(content, PagedModelBook.class);
        List<BookVO> books = wrapper.getContent();

        BookVO foundBookOne = books.get(0);

        Assertions.assertNotNull(foundBookOne.getId());
        Assertions.assertNotNull(foundBookOne.getTitle());
        Assertions.assertNotNull(foundBookOne.getAuthor());
        Assertions.assertNotNull(foundBookOne.getPrice());
        Assertions.assertTrue(foundBookOne.getId() > 0);
        Assertions.assertEquals("Big Data: como extrair volume, variedade, velocidade e valor da avalanche de informação cotidiana", foundBookOne.getTitle());
        Assertions.assertEquals("Viktor Mayer-Schonberger e Kenneth Kukier", foundBookOne.getAuthor());
        Assertions.assertEquals(54.00, foundBookOne.getPrice());

        BookVO foundBookFive = books.get(4);

        Assertions.assertNotNull(foundBookFive.getId());
        Assertions.assertNotNull(foundBookFive.getTitle());
        Assertions.assertNotNull(foundBookFive.getAuthor());
        Assertions.assertNotNull(foundBookFive.getPrice());
        Assertions.assertTrue(foundBookFive.getId() > 0);
        Assertions.assertEquals("Domain Driven Design", foundBookFive.getTitle());
        Assertions.assertEquals("Eric Evans", foundBookFive.getAuthor());
        Assertions.assertEquals(92.00, foundBookFive.getPrice());
    }


    @Test
    @Order(6)
    public void testFindAllWithoutToken() {

        RequestSpecification specificationWithoutToken = new RequestSpecBuilder()
                .setBasePath("/api/book/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();

        given().spec(specificationWithoutToken)
                .contentType(TestConfigs.CONTENT_TYPE_XML)
                .accept(TestConfigs.CONTENT_TYPE_XML)
                .when()
                .get()
                .then()
                .statusCode(403);
    }

    @Test
    @Order(7)
    public void testHATEOAS() {

        var content = given().spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_XML)
                .accept(TestConfigs.CONTENT_TYPE_XML)
                .queryParams("page", 0 , "size", 12, "direction", "asc")
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        Assertions.assertTrue(content.contains("<links><rel>self</rel><href>http://localhost:8888/api/book/v1/3</href></links>"));
        Assertions.assertTrue(content.contains("<links><rel>self</rel><href>http://localhost:8888/api/book/v1/5</href></links>"));
        Assertions.assertTrue(content.contains("<links><rel>self</rel><href>http://localhost:8888/api/book/v1/7</href></links>"));

        Assertions.assertTrue(content.contains("<links><rel>first</rel><href>http://localhost:8888/api/book/v1?direction=asc&amp;page=0&amp;size=12&amp;sort=title,asc</href></links>"));
        Assertions.assertTrue(content.contains("<links><rel>self</rel><href>http://localhost:8888/api/book/v1?page=0&amp;size=12&amp;direction=asc</href></links>"));
        Assertions.assertTrue(content.contains("<links><rel>next</rel><href>http://localhost:8888/api/book/v1?direction=asc&amp;page=1&amp;size=12&amp;sort=title,asc</href></links>"));
        Assertions.assertTrue(content.contains("<links><rel>last</rel><href>http://localhost:8888/api/book/v1?direction=asc&amp;page=1&amp;size=12&amp;sort=title,asc</href></links>"));

        Assertions.assertTrue(content.contains("<page><size>12</size><totalElements>15</totalElements><totalPages>2</totalPages><number>0</number></page>"));
    }

    private void mockBook() {
        book.setTitle("Docker Deep Dive");
        book.setAuthor("Nigel Poulton");
        book.setPrice(55.99);
        book.setLaunchDate(new Date());
    }
}