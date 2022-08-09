package br.com.caduartioli.restspringboot3.services;

import br.com.caduartioli.restspringboot3.controllers.BookController;
import br.com.caduartioli.restspringboot3.controllers.PersonController;
import br.com.caduartioli.restspringboot3.data.vo.v1.BookVO;
import br.com.caduartioli.restspringboot3.data.vo.v1.PersonVO;
import br.com.caduartioli.restspringboot3.exceptions.ResourceNotFoundException;
import br.com.caduartioli.restspringboot3.exceptions.ResourceObjectIsNullException;
import br.com.caduartioli.restspringboot3.mapper.DozerMapper;
import br.com.caduartioli.restspringboot3.model.Book;
import br.com.caduartioli.restspringboot3.repositories.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class BookServices {
    private final Logger logger = Logger.getLogger(BookServices.class.getName());

    @Autowired
    BookRepository repository;

    @Autowired
    PagedResourcesAssembler<BookVO> assembler;

    public PagedModel<EntityModel<BookVO>> findAll(Pageable pageable) {
        logger.info("Finding all books!");

        var bookPage = repository.findAll(pageable);

        var bookVosPage = bookPage.map(book -> DozerMapper.parseObject(book, BookVO.class));
        bookVosPage.map(bookVO -> bookVO.add(linkTo(methodOn(BookController.class).findById(bookVO.getKey())).withSelfRel()));

        Link link = linkTo(methodOn(BookController.class)
                .findAll(pageable.getPageNumber(), pageable.getPageSize(), "asc"))
                .withSelfRel();
        return assembler.toModel(bookVosPage, link);
    }

    public BookVO findById(Long id) {
        logger.info("Finding one book!");

        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));

        var vo = DozerMapper.parseObject(entity, BookVO.class);
        vo.add(linkTo(methodOn(BookController.class).findById(id)).withSelfRel());
        return vo;
    }

    public BookVO create(BookVO book) {

        if (book == null) throw new ResourceObjectIsNullException();

        logger.info("Creating one book!");
        var entity = DozerMapper.parseObject(book, Book.class);
        var vo = DozerMapper.parseObject(repository.save(entity), BookVO.class);
        vo.add(linkTo(methodOn(BookController.class).findById(vo.getKey())).withSelfRel());
        return vo;
    }

    public BookVO update(BookVO book) {

        if (book == null) throw new ResourceObjectIsNullException();

        logger.info("Updating one book!");

        var entity = repository.findById(book.getKey())
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));

        entity.setAuthor(book.getAuthor());
        entity.setLaunchDate(book.getLaunchDate());
        entity.setPrice(book.getPrice());
        entity.setTitle(book.getTitle());

        var vo = DozerMapper.parseObject(repository.save(entity), BookVO.class);
        vo.add(linkTo(methodOn(BookController.class).findById(vo.getKey())).withSelfRel());
        return vo;
    }

    public void delete(Long id) {
        logger.info("Deleting one book!");

        Book entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));

        repository.delete(entity);
    }
}
