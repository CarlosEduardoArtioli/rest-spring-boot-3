package br.com.caduartioli.restspringboot3.repositories;

import br.com.caduartioli.restspringboot3.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {



}
