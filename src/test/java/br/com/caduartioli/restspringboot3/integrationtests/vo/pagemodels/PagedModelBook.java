package br.com.caduartioli.restspringboot3.integrationtests.vo.pagemodels;

import br.com.caduartioli.restspringboot3.integrationtests.vo.BookVO;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;
import java.util.Objects;

@XmlRootElement
public class PagedModelBook {

    @XmlElement
    private List<BookVO> content;

    public PagedModelBook() {
    }

    public PagedModelBook(List<BookVO> content) {
        this.content = content;
    }

    public List<BookVO> getContent() {
        return content;
    }

    public void setContent(List<BookVO> content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PagedModelBook that = (PagedModelBook) o;
        return Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content);
    }
}
