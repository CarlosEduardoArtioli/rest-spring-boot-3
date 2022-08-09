package br.com.caduartioli.restspringboot3.integrationtests.vo.pagemodels;

import br.com.caduartioli.restspringboot3.integrationtests.vo.PersonVO;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;
import java.util.Objects;

@XmlRootElement
public class PagedModelPerson {

    @XmlElement
    private List<PersonVO> content;

    public PagedModelPerson() {
    }

    public PagedModelPerson(List<PersonVO> content) {
        this.content = content;
    }

    public List<PersonVO> getContent() {
        return content;
    }

    public void setContent(List<PersonVO> content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PagedModelPerson that = (PagedModelPerson) o;
        return Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content);
    }
}
