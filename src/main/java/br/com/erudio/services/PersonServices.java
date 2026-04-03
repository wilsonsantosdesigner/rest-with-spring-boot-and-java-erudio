package br.com.erudio.services;

import br.com.erudio.controllers.PersonController;
import br.com.erudio.data.dto.PersonDTO;
import br.com.erudio.exception.RequiredObjectIsNullException;
import br.com.erudio.exception.ResourceNotFoundException;
import br.com.erudio.model.Person;
import br.com.erudio.repository.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import org.springframework.stereotype.Service;

import java.util.List;

import static br.com.erudio.mapper.ObjectMapper.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class PersonServices {

    private final Logger logger = LoggerFactory.getLogger(PersonServices.class.getName());

    @Autowired
    PersonRepository repository;

    public List<PersonDTO> findAll() {
        logger.info("Finding all People!");

        var persons = parseObjects(repository.findAll(), PersonDTO.class);

        //persons.forEach(this::addHateoasLinks);

        for (PersonDTO person : persons) {
            addHateoasLinks(person);
        }

        return persons;
    }

    public PersonDTO findById(Long id) {
        logger.info("Buscando uma pessoa!");

        var person = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Não foi encontrado Pessoa com o ID: " + id));
        var personDTO = parseObject(person, PersonDTO.class);

        addHateoasLinks(personDTO);

        return personDTO;
    }

    public PersonDTO create(PersonDTO personDTO) {

        if(personDTO == null) throw  new RequiredObjectIsNullException();

        logger.info("Creating one Person!");
        var entity = parseObject(personDTO, Person.class);

        var dto = parseObject(repository.save(entity), PersonDTO.class);

        addHateoasLinks(dto);

        return dto;
    }

    public PersonDTO update(PersonDTO person) {

        if(person == null) throw  new RequiredObjectIsNullException();

        logger.info("Updating one Person!");
        Person entity = repository.findById(person.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));

        entity.setFirstName(person.getFirstName());
        entity.setLastName(person.getLastName());
        entity.setAddress(person.getAddress());
        entity.setGender(person.getGender());

        var dto = parseObject(repository.save(entity), PersonDTO.class);
        addHateoasLinks(dto);
        return dto;
    }

    public void delete(Long id) {

        logger.info("Deleting one Person!");

        Person entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));
        repository.delete(entity);
    }

    private void addHateoasLinks(PersonDTO personDTO) {
        personDTO.add(linkTo(methodOn(PersonController.class).findById(personDTO.getId())).withSelfRel().withType("GET"));

        personDTO.add(linkTo(methodOn(PersonController.class).findAll()).withRel("findAll").withType("GET"));

        personDTO.add(linkTo(methodOn(PersonController.class).create(personDTO)).withRel("create").withType("POST"));

        personDTO.add(linkTo(methodOn(PersonController.class).update(personDTO)).withRel("update").withType("PUT"));

        personDTO.add(linkTo(methodOn(PersonController.class).delete(personDTO.getId())).withRel("delete").withType("DELETE"));

    }
}