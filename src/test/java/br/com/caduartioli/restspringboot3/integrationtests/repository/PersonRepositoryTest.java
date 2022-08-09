package br.com.caduartioli.restspringboot3.integrationtests.repository;

import br.com.caduartioli.restspringboot3.integrationtests.testcontainers.AbstractIntegrationTest;
import br.com.caduartioli.restspringboot3.model.Person;
import br.com.caduartioli.restspringboot3.repositories.PersonRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PersonRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    PersonRepository repository;

    private static Person person;

    @BeforeAll
    public static void setup() {
        person = new Person();
    }

    @Test
    @Order(0)
    public void testFindByName() {

        Pageable pageable = PageRequest.of(0, 6, Sort.by(Sort.Direction.ASC, "firstName"));
        person = repository.findPersonsByName("ay", pageable).getContent().get(1);

        Assertions.assertNotNull(person.getId());
        Assertions.assertNotNull(person.getFirstName());
        Assertions.assertNotNull(person.getLastName());
        Assertions.assertNotNull(person.getAddress());
        Assertions.assertNotNull(person.getGender());

        Assertions.assertTrue(person.getEnabled());

        Assertions.assertEquals(647, person.getId());

        Assertions.assertEquals("Dayle", person.getFirstName());
        Assertions.assertEquals("Hrachovec", person.getLastName());
        Assertions.assertEquals("9823 Rockefeller Circle", person.getAddress());
        Assertions.assertEquals("Female", person.getGender());
    }

    @Test
    @Order(1)
    public void testDisablePerson() {

        repository.disablePerson(person.getId());

        Pageable pageable = PageRequest.of(0, 6, Sort.by(Sort.Direction.ASC, "firstName"));
        person = repository.findPersonsByName("ay", pageable).getContent().get(1);

        Assertions.assertNotNull(person.getId());
        Assertions.assertNotNull(person.getFirstName());
        Assertions.assertNotNull(person.getLastName());
        Assertions.assertNotNull(person.getAddress());
        Assertions.assertNotNull(person.getGender());

        Assertions.assertFalse(person.getEnabled());

        Assertions.assertEquals(647, person.getId());

        Assertions.assertEquals("Dayle", person.getFirstName());
        Assertions.assertEquals("Hrachovec", person.getLastName());
        Assertions.assertEquals("9823 Rockefeller Circle", person.getAddress());
        Assertions.assertEquals("Female", person.getGender());
    }
}
