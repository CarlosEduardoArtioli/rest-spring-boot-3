package br.com.caduartioli.restspringboot3.integrationtests.controller.withjson;

import br.com.caduartioli.restspringboot3.configs.TestConfigs;
import br.com.caduartioli.restspringboot3.integrationtests.testcontainers.AbstractIntegrationTest;
import br.com.caduartioli.restspringboot3.integrationtests.vo.PersonVO;
import br.com.caduartioli.restspringboot3.integrationtests.vo.security.AccountCredentialsVO;
import br.com.caduartioli.restspringboot3.integrationtests.vo.security.TokenVO;
import br.com.caduartioli.restspringboot3.integrationtests.vo.wrappers.WrapperPersonVO;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.DeserializationFeature;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PersonControllerJsonTest extends AbstractIntegrationTest {

    private static RequestSpecification specification;
    private static ObjectMapper objectMapper;

    private static PersonVO person;

    @BeforeAll
    public static void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        person = new PersonVO();
    }

    @Test
    @Order(0)
    public void authorization() {

        AccountCredentialsVO user = new AccountCredentialsVO("leandro", "admin123");

        var accessToken = given()
                .basePath("/auth/signin")
                .port(TestConfigs.SERVER_PORT)
                .contentType(TestConfigs.CONTENT_TYPE_JSON)
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
                .setBasePath("/api/person/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
    }

    @Test
    @Order(1)
    public void testCreate() throws IOException {
        mockPerson();

        var content = given().spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_JSON)
                .body(person)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        PersonVO persistedPerson = objectMapper.readValue(content, PersonVO.class);
        person = persistedPerson;

        Assertions.assertNotNull(persistedPerson);

        Assertions.assertNotNull(persistedPerson.getId());
        Assertions.assertNotNull(persistedPerson.getFirstName());
        Assertions.assertNotNull(persistedPerson.getLastName());
        Assertions.assertNotNull(persistedPerson.getAddress());
        Assertions.assertNotNull(persistedPerson.getGender());
        Assertions.assertTrue(persistedPerson.getEnabled());

        Assertions.assertTrue(persistedPerson.getId() > 0);

        Assertions.assertEquals("Nelson", persistedPerson.getFirstName());
        Assertions.assertEquals("Piquet", persistedPerson.getLastName());
        Assertions.assertEquals("Brasília - DF - Brasil", persistedPerson.getAddress());
        Assertions.assertEquals("Male", persistedPerson.getGender());
    }

    @Test
    @Order(2)
    public void testUpdate() throws IOException {
        person.setLastName("Piquet Souto Maior");

        var content = given().spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_JSON)
                .body(person)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        PersonVO persistedPerson = objectMapper.readValue(content, PersonVO.class);
        person = persistedPerson;

        Assertions.assertNotNull(persistedPerson);

        Assertions.assertNotNull(persistedPerson.getId());
        Assertions.assertNotNull(persistedPerson.getFirstName());
        Assertions.assertNotNull(persistedPerson.getLastName());
        Assertions.assertNotNull(persistedPerson.getAddress());
        Assertions.assertNotNull(persistedPerson.getGender());
        Assertions.assertTrue(persistedPerson.getEnabled());

        Assertions.assertEquals(person.getId(), persistedPerson.getId());

        Assertions.assertEquals("Nelson", persistedPerson.getFirstName());
        Assertions.assertEquals("Piquet Souto Maior", persistedPerson.getLastName());
        Assertions.assertEquals("Brasília - DF - Brasil", persistedPerson.getAddress());
        Assertions.assertEquals("Male", persistedPerson.getGender());
    }

    @Test
    @Order(3)
    public void testDisablePersonById() throws IOException {
        var content = given().spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_JSON)
                .pathParam("id", person.getId())
                .when()
                .patch("{id}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        PersonVO persistedPerson = objectMapper.readValue(content, PersonVO.class);
        person = persistedPerson;

        Assertions.assertNotNull(persistedPerson);

        Assertions.assertNotNull(persistedPerson.getId());
        Assertions.assertNotNull(persistedPerson.getFirstName());
        Assertions.assertNotNull(persistedPerson.getLastName());
        Assertions.assertNotNull(persistedPerson.getAddress());
        Assertions.assertNotNull(persistedPerson.getGender());
        Assertions.assertFalse(persistedPerson.getEnabled());

        Assertions.assertEquals(person.getId(), persistedPerson.getId());

        Assertions.assertEquals("Nelson", persistedPerson.getFirstName());
        Assertions.assertEquals("Piquet Souto Maior", persistedPerson.getLastName());
        Assertions.assertEquals("Brasília - DF - Brasil", persistedPerson.getAddress());
        Assertions.assertEquals("Male", persistedPerson.getGender());
    }

    @Test
    @Order(4)
    public void testFindById() throws IOException {
        mockPerson();

        var content = given().spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_JSON)
                .pathParam("id", person.getId())
                .when()
                .get("{id}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        PersonVO persistedPerson = objectMapper.readValue(content, PersonVO.class);
        person = persistedPerson;

        Assertions.assertNotNull(persistedPerson);

        Assertions.assertNotNull(persistedPerson.getId());
        Assertions.assertNotNull(persistedPerson.getFirstName());
        Assertions.assertNotNull(persistedPerson.getLastName());
        Assertions.assertNotNull(persistedPerson.getAddress());
        Assertions.assertNotNull(persistedPerson.getGender());
        Assertions.assertFalse(persistedPerson.getEnabled());

        Assertions.assertEquals(person.getId(), persistedPerson.getId());

        Assertions.assertEquals("Nelson", persistedPerson.getFirstName());
        Assertions.assertEquals("Piquet Souto Maior", persistedPerson.getLastName());
        Assertions.assertEquals("Brasília - DF - Brasil", persistedPerson.getAddress());
        Assertions.assertEquals("Male", persistedPerson.getGender());
    }

    @Test
    @Order(5)
    public void testDelete() {

        given().spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_JSON)
                .pathParam("id", person.getId())
                .when()
                .delete("{id}")
                .then()
                .statusCode(204);
    }

    @Test
    @Order(6)
    public void testFindAll() throws IOException {

        var content = given().spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_JSON)
                .accept(TestConfigs.CONTENT_TYPE_JSON)
                .queryParams("page", 3, "size", 10, "direction", "asc")
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        WrapperPersonVO wrapper  = objectMapper.readValue(content, WrapperPersonVO.class);
        var people = wrapper.getEmbedded().getPersons();

        PersonVO foundPersonOne = people.get(0);

        Assertions.assertNotNull(foundPersonOne.getId());
        Assertions.assertNotNull(foundPersonOne.getFirstName());
        Assertions.assertNotNull(foundPersonOne.getLastName());
        Assertions.assertNotNull(foundPersonOne.getAddress());
        Assertions.assertNotNull(foundPersonOne.getGender());
        Assertions.assertTrue(foundPersonOne.getEnabled());

        Assertions.assertEquals(676, foundPersonOne.getId());

        Assertions.assertEquals("Alic", foundPersonOne.getFirstName());
        Assertions.assertEquals("Terbrug", foundPersonOne.getLastName());
        Assertions.assertEquals("3 Eagle Crest Court", foundPersonOne.getAddress());
        Assertions.assertEquals("Male", foundPersonOne.getGender());

        PersonVO foundPersonSix = people.get(5);

        Assertions.assertNotNull(foundPersonSix.getId());
        Assertions.assertNotNull(foundPersonSix.getFirstName());
        Assertions.assertNotNull(foundPersonSix.getLastName());
        Assertions.assertNotNull(foundPersonSix.getAddress());
        Assertions.assertNotNull(foundPersonSix.getGender());

        Assertions.assertTrue(foundPersonSix.getEnabled());

        Assertions.assertEquals(910, foundPersonSix.getId());

        Assertions.assertEquals("Allegra", foundPersonSix.getFirstName());
        Assertions.assertEquals("Dome", foundPersonSix.getLastName());
        Assertions.assertEquals("57 Roxbury Pass", foundPersonSix.getAddress());
        Assertions.assertEquals("Female", foundPersonSix.getGender());
    }

    @Test
    @Order(7)
    public void testFindByName() throws IOException {

        var content = given().spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_JSON)
                .accept(TestConfigs.CONTENT_TYPE_JSON)
                .pathParam("firstName", "ay")
                .queryParams("page", 0, "size", 6, "direction", "asc")
                .when()
                .get("/findPersonByName/{firstName}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        WrapperPersonVO wrapper = objectMapper.readValue(content, WrapperPersonVO.class);
        var people = wrapper.getEmbedded().getPersons();

        PersonVO foundPersonOne = people.get(0);

        Assertions.assertNotNull(foundPersonOne.getId());
        Assertions.assertNotNull(foundPersonOne.getFirstName());
        Assertions.assertNotNull(foundPersonOne.getLastName());
        Assertions.assertNotNull(foundPersonOne.getAddress());
        Assertions.assertNotNull(foundPersonOne.getGender());

        Assertions.assertFalse(foundPersonOne.getEnabled());

        Assertions.assertEquals(291, foundPersonOne.getId());

        Assertions.assertEquals("Clayton", foundPersonOne.getFirstName());
        Assertions.assertEquals("Matveiko", foundPersonOne.getLastName());
        Assertions.assertEquals("92 Sunfield Street", foundPersonOne.getAddress());
        Assertions.assertEquals("Male", foundPersonOne.getGender());
    }

    @Test
    @Order(8)
    public void testFindAllWithoutToken() {

        RequestSpecification specificationWithoutToken = new RequestSpecBuilder()
                .setBasePath("/api/person/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();

        given().spec(specificationWithoutToken)
                .contentType(TestConfigs.CONTENT_TYPE_JSON)
                .when()
                .get()
                .then()
                .statusCode(403);
    }

    @Test
    @Order(9)
    public void testHATEOAS() {

        var content = given().spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_JSON)
                .accept(TestConfigs.CONTENT_TYPE_JSON)
                .queryParams("page", 3, "size", 10, "direction", "asc")
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        Assertions.assertTrue(content.contains("\"_links\":{\"self\":{\"href\":\"http://localhost:8888/api/person/v1/676\"}}}"));
        Assertions.assertTrue(content.contains("\"_links\":{\"self\":{\"href\":\"http://localhost:8888/api/person/v1/845\"}}}"));
        Assertions.assertTrue(content.contains("\"_links\":{\"self\":{\"href\":\"http://localhost:8888/api/person/v1/713\"}}}"));

        Assertions.assertTrue(content.contains("{\"first\":{\"href\":\"http://localhost:8888/api/person/v1?direction=asc&page=0&size=10&sort=firstName,asc\"}"));
        Assertions.assertTrue(content.contains("\"prev\":{\"href\":\"http://localhost:8888/api/person/v1?direction=asc&page=2&size=10&sort=firstName,asc\"}"));
        Assertions.assertTrue(content.contains("\"self\":{\"href\":\"http://localhost:8888/api/person/v1?page=3&size=10&direction=asc\"}"));
        Assertions.assertTrue(content.contains("\"next\":{\"href\":\"http://localhost:8888/api/person/v1?direction=asc&page=4&size=10&sort=firstName,asc\"}"));
        Assertions.assertTrue(content.contains("\"last\":{\"href\":\"http://localhost:8888/api/person/v1?direction=asc&page=100&size=10&sort=firstName,asc\"}}"));

        Assertions.assertTrue(content.contains("\"page\":{\"size\":10,\"totalElements\":1005,\"totalPages\":101,\"number\":3}}"));
    }

    private void mockPerson() {
        person.setFirstName("Nelson");
        person.setLastName("Piquet");
        person.setAddress("Brasília - DF - Brasil");
        person.setGender("Male");
        person.setEnabled(true);
    }
}
