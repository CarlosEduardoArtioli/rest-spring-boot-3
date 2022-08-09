package br.com.caduartioli.restspringboot3.integrationtests.vo.wrappers;

import br.com.caduartioli.restspringboot3.integrationtests.vo.BookVO;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class BookEmbeddedVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("bookVOList")
    private List<BookVO> books;

    public BookEmbeddedVO() {
    }

    public List<BookVO> getBooks() {
        return books;
    }

    public void setBooks(List<BookVO> books) {
        this.books = books;
    }
}