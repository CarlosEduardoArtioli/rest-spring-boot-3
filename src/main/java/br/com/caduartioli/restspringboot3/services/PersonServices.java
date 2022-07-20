package br.com.caduartioli.restspringboot3.services;

import br.com.caduartioli.restspringboot3.data.vo.v1.PersonVO;
import br.com.caduartioli.restspringboot3.exceptions.ResourceNotFoundException;
import br.com.caduartioli.restspringboot3.mapper.DozerMapper;
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

    public List<PersonVO> findAll() {
        logger.info("Finding all people!");

        return DozerMapper.parseListObjects(personRepository.findAll(), PersonVO.class);
    }

    public PersonVO findById(Long id) {
        logger.info("Finding one person!");

        var entity = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));

        return DozerMapper.parseObject(entity, PersonVO.class);
    }

    public PersonVO create(PersonVO person) {
        logger.info("Creating one person!");

        var entity = DozerMapper.parseObject(person, Person.class);

        var vo = personRepository.save(entity);

        return DozerMapper.parseObject(vo, PersonVO.class);
    }

    public PersonVO update(PersonVO person) {
        logger.info("Updating one person!");

        var entity = personRepository.findById(person.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));

        entity.setFirstName(person.getFirstName());
        entity.setLastName(person.getLastName());
        entity.setAddress(person.getAddress());
        entity.setGender(person.getGender());

        var vo = personRepository.save(entity);

        return DozerMapper.parseObject(vo, PersonVO.class);
    }

    public void delete(Long id) {
        logger.info("Deleting one person!");

        Person entity = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));

        personRepository.delete(entity);
    }
}
