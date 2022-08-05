package br.com.caduartioli.restspringboot3.integrationtests.controller.withjson;

import br.com.caduartioli.restspringboot3.configs.TestConfigs;
import br.com.caduartioli.restspringboot3.integrationtests.testcontainers.AbstractIntegrationTest;
import br.com.caduartioli.restspringboot3.integrationtests.vo.PersonVO;
import br.com.caduartioli.restspringboot3.integrationtests.vo.security.AccountCredentialsVO;
import br.com.caduartioli.restspringboot3.integrationtests.vo.security.TokenVO;
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

        Assertions.assertEquals(person.getId(), persistedPerson.getId());

        Assertions.assertEquals("Nelson", persistedPerson.getFirstName());
        Assertions.assertEquals("Piquet Souto Maior", persistedPerson.getLastName());
        Assertions.assertEquals("Brasília - DF - Brasil", persistedPerson.getAddress());
        Assertions.assertEquals("Male", persistedPerson.getGender());
    }

    @Test
    @Order(3)
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

        Assertions.assertEquals(person.getId(), persistedPerson.getId());

        Assertions.assertEquals("Nelson", persistedPerson.getFirstName());
        Assertions.assertEquals("Piquet Souto Maior", persistedPerson.getLastName());
        Assertions.assertEquals("Brasília - DF - Brasil", persistedPerson.getAddress());
        Assertions.assertEquals("Male", persistedPerson.getGender());
    }

    @Test
    @Order(4)
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
    @Order(5)
    public void testFindAll() throws IOException {

        var content = given().spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_JSON)
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        List<PersonVO> people = objectMapper.readValue(content, new TypeReference<List<PersonVO>>() {
        });

        PersonVO foundPersonOne = people.get(0);

        Assertions.assertNotNull(foundPersonOne.getId());
        Assertions.assertNotNull(foundPersonOne.getFirstName());
        Assertions.assertNotNull(foundPersonOne.getLastName());
        Assertions.assertNotNull(foundPersonOne.getAddress());
        Assertions.assertNotNull(foundPersonOne.getGender());

        Assertions.assertEquals(1, foundPersonOne.getId());

        Assertions.assertEquals("Ayrton", foundPersonOne.getFirstName());
        Assertions.assertEquals("Senna", foundPersonOne.getLastName());
        Assertions.assertEquals("São Paulo", foundPersonOne.getAddress());
        Assertions.assertEquals("Male", foundPersonOne.getGender());

        PersonVO foundPersonSix = people.get(5);

        Assertions.assertNotNull(foundPersonSix.getId());
        Assertions.assertNotNull(foundPersonSix.getFirstName());
        Assertions.assertNotNull(foundPersonSix.getLastName());
        Assertions.assertNotNull(foundPersonSix.getAddress());
        Assertions.assertNotNull(foundPersonSix.getGender());

        Assertions.assertEquals(9, foundPersonSix.getId());

        Assertions.assertEquals("Nelson", foundPersonSix.getFirstName());
        Assertions.assertEquals("Mvezo", foundPersonSix.getLastName());
        Assertions.assertEquals("Mvezo – South Africa", foundPersonSix.getAddress());
        Assertions.assertEquals("Male", foundPersonSix.getGender());
    }


    @Test
    @Order(6)
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

    private void mockPerson() {
        person.setFirstName("Nelson");
        person.setLastName("Piquet");
        person.setAddress("Brasília - DF - Brasil");
        person.setGender("Male");
    }
}
