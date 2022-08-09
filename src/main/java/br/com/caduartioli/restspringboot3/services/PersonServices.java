package br.com.caduartioli.restspringboot3.services;

import br.com.caduartioli.restspringboot3.controllers.PersonController;
import br.com.caduartioli.restspringboot3.data.vo.v1.PersonVO;
import br.com.caduartioli.restspringboot3.exceptions.ResourceNotFoundException;
import br.com.caduartioli.restspringboot3.exceptions.ResourceObjectIsNullException;
import br.com.caduartioli.restspringboot3.mapper.DozerMapper;
import br.com.caduartioli.restspringboot3.model.Person;
import br.com.caduartioli.restspringboot3.repositories.PersonRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class PersonServices {
    private final Logger logger = Logger.getLogger(PersonServices.class.getName());

    @Autowired
    PersonRepository repository;

    @Autowired
    PagedResourcesAssembler<PersonVO> assembler;

    public PagedModel<EntityModel<PersonVO>> findAll(Pageable pageable) {
        logger.info("Finding all people!");

        var personPage = repository.findAll(pageable);

        var personVosPage = personPage.map(person -> DozerMapper.parseObject(person, PersonVO.class));
        personVosPage.map(personVO -> personVO.add(linkTo(methodOn(PersonController.class).findById(personVO.getKey())).withSelfRel()));

        Link link = linkTo(methodOn(PersonController.class)
                .findAll(pageable.getPageNumber(), pageable.getPageSize(), "asc"))
                .withSelfRel();
        return assembler.toModel(personVosPage, link);
    }

    public PagedModel<EntityModel<PersonVO>> findPersonByName(String firstName, Pageable pageable) {
        logger.info("Finding persons by name!");

        var personPage = repository.findPersonsByName(firstName, pageable);

        var personVosPage = personPage.map(person -> DozerMapper.parseObject(person, PersonVO.class));
        personVosPage.map(personVO -> personVO.add(linkTo(methodOn(PersonController.class).findById(personVO.getKey())).withSelfRel()));

        Link link = linkTo(methodOn(PersonController.class)
                .findAll(pageable.getPageNumber(), pageable.getPageSize(), "asc"))
                .withSelfRel();
        return assembler.toModel(personVosPage, link);
    }

    public PersonVO findById(Long id) {
        logger.info("Finding one person!");

        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));

        var vo = DozerMapper.parseObject(entity, PersonVO.class);
        vo.add(linkTo(methodOn(PersonController.class).findById(id)).withSelfRel());
        return vo;
    }

    public PersonVO create(PersonVO person) {

        if (person == null) throw new ResourceObjectIsNullException();

        logger.info("Creating one person!");
        var entity = DozerMapper.parseObject(person, Person.class);
        var vo = DozerMapper.parseObject(repository.save(entity), PersonVO.class);
        vo.add(linkTo(methodOn(PersonController.class).findById(vo.getKey())).withSelfRel());
        return vo;
    }

    public PersonVO update(PersonVO person) {

        if (person == null) throw new ResourceObjectIsNullException();

        logger.info("Updating one person!");

        var entity = repository.findById(person.getKey())
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));

        entity.setFirstName(person.getFirstName());
        entity.setLastName(person.getLastName());
        entity.setAddress(person.getAddress());
        entity.setGender(person.getGender());

        var vo = DozerMapper.parseObject(repository.save(entity), PersonVO.class);
        vo.add(linkTo(methodOn(PersonController.class).findById(vo.getKey())).withSelfRel());
        return vo;
    }

    @Transactional
    public PersonVO disablePerson(Long id) {
        logger.info("Disabling one person!");

        repository.disablePerson(id);

        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));

        var vo = DozerMapper.parseObject(entity, PersonVO.class);
        vo.add(linkTo(methodOn(PersonController.class).findById(id)).withSelfRel());
        return vo;
    }

    public void delete(Long id) {
        logger.info("Deleting one person!");

        Person entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));

        repository.delete(entity);
    }
}
