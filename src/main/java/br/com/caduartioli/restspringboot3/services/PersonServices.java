package br.com.caduartioli.restspringboot3.services;

import br.com.caduartioli.restspringboot3.exceptions.ResourceNotFoundException;
import br.com.caduartioli.restspringboot3.model.Person;
import br.com.caduartioli.restspringboot3.repositories.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class PersonServices {
    private Logger logger = Logger.getLogger(PersonServices.class.getName());

    @Autowired
    PersonRepository personRepository;

    public List<Person> findAll() {
        logger.info("Finding all people!");

        return personRepository.findAll();
    }

    public Person findById(Long id) {
        logger.info("Finding one person!");

        return personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));
    }

    public Person create(Person person) {
        logger.info("Creating one person!");

        return personRepository.save(person);
    }

    public Person update(Person person) {
        logger.info("Updating one person!");

        Person entity = personRepository.findById(person.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));

        entity.setFirstName(person.getFirstName());
        entity.setLastName(person.getLastName());
        entity.setAddress(person.getAddress());
        entity.setGender(person.getGender());

        return personRepository.save(entity);
    }

    public void delete(Long id) {
        logger.info("Deleting one person!");

        Person entity = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));

        personRepository.delete(entity);
    }
}
