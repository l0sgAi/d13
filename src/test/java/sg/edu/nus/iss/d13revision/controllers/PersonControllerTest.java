package sg.edu.nus.iss.d13revision.controllers;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import sg.edu.nus.iss.d13revision.models.Person;
import sg.edu.nus.iss.d13revision.services.PersonService;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PersonController.class)
public class PersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PersonService personSvc;

    @Test
    public void indexReturnsIndexViewWithWelcomeMessage() throws Exception {
        mockMvc.perform(get("/person/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("message", "Spring Boot & Thymeleaf Revision"));
    }

    @Test
    public void getAllPersonsReturnsPersonsAsJson() throws Exception {
        when(personSvc.getPersons()).thenReturn(List.of(
                new Person("abc12345", "Mark", "Zuckerberg"),
                new Person("def67890", "Elon", "Musk")));

        mockMvc.perform(get("/person/testRetrieve"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("abc12345"))
                .andExpect(jsonPath("$[0].firstName").value("Mark"))
                .andExpect(jsonPath("$[0].lastName").value("Zuckerberg"))
                .andExpect(jsonPath("$[1].firstName").value("Elon"));

        verify(personSvc).getPersons();
    }

    @Test
    public void personListReturnsPersonListViewWithPersons() throws Exception {
        when(personSvc.getPersons()).thenReturn(List.of(
                new Person("Mark", "Zuckerberg")));

        mockMvc.perform(get("/person/personList"))
                .andExpect(status().isOk())
                .andExpect(view().name("personList"))
                .andExpect(model().attributeExists("persons"))
                .andExpect(model().attribute("persons", hasSize(1)));
    }

    @Test
    public void showAddPersonPageReturnsAddPersonViewWithForm() throws Exception {
        mockMvc.perform(get("/person/addPerson"))
                .andExpect(status().isOk())
                .andExpect(view().name("addPerson"))
                .andExpect(model().attributeExists("personForm"));
    }

    @Test
    public void savePersonWithValidFormRedirectsToList() throws Exception {
        mockMvc.perform(post("/person/addPerson")
                        .param("firstName", "Bill")
                        .param("lastName", "Gates"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/person/personList"));

        verify(personSvc).addPerson(any(Person.class));
    }

    @Test
    public void savePersonWithEmptyFirstNameReturnsAddPersonViewWithError() throws Exception {
        mockMvc.perform(post("/person/addPerson")
                        .param("firstName", "")
                        .param("lastName", "Gates"))
                .andExpect(status().isOk())
                .andExpect(view().name("addPerson"))
                .andExpect(model().attribute("errorMessage",
                        "First Name & Last Name are required!"));

        Mockito.verifyNoInteractions(personSvc);
    }

    @Test
    public void savePersonWithNullLastNameReturnsAddPersonViewWithError() throws Exception {
        mockMvc.perform(post("/person/addPerson")
                        .param("firstName", "Bill"))
                .andExpect(status().isOk())
                .andExpect(view().name("addPerson"))
                .andExpect(model().attribute("errorMessage",
                        "First Name & Last Name are required!"));

        Mockito.verifyNoInteractions(personSvc);
    }

    @Test
    public void personToEditReturnsEditPersonViewWithPerson() throws Exception {
        mockMvc.perform(post("/person/personToEdit")
                        .param("id", "abc12345")
                        .param("firstName", "Mark")
                        .param("lastName", "Zuckerberg"))
                .andExpect(status().isOk())
                .andExpect(view().name("editPerson"))
                .andExpect(model().attributeExists("per"))
                .andExpect(model().attribute("per",
                        org.hamcrest.Matchers.hasProperty("firstName", org.hamcrest.Matchers.is("Mark"))));
    }

    @Test
    public void personEditUpdatesPersonAndRedirectsToList() throws Exception {
        mockMvc.perform(post("/person/personEdit")
                        .param("id", "abc12345")
                        .param("firstName", "Mark")
                        .param("lastName", "Z"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/person/personList"));

        verify(personSvc).updatePerson(any(Person.class));
    }

    @Test
    public void personDeleteRemovesPersonAndRedirectsToList() throws Exception {
        mockMvc.perform(post("/person/personDelete")
                        .param("id", "abc12345")
                        .param("firstName", "Mark")
                        .param("lastName", "Zuckerberg"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/person/personList"));

        verify(personSvc).removePerson(any(Person.class));
    }
}
