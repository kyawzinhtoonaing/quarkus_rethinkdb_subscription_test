package org.acme;

import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import org.acme.model.dto.CreatePersonDto;
import org.acme.model.dto.PersonRangeFilterDto;
import org.acme.model.viewonly.PersonView;
import org.acme.service.PersonService;
import org.eclipse.microprofile.graphql.*;

import java.util.List;

@GraphQLApi
public class PersonGraphQLResource {
    private final PersonService personService;

    PersonGraphQLResource(PersonService personService) {
        this.personService = personService;
    }

    @Mutation("createPerson")
    public boolean createPerson(@NonNull CreatePersonDto person) {
        personService.createPersonView(person);
        return true;
    }

    @Subscription("subscribePersonViewNotification")
    public Multi<PersonView> subscribePersonViewNotification() {
        return personService.getPersonViewNotification();
    }

    @Subscription("subscribeFilteredPersonViewNotification")
    public Multi<List<PersonView>> subscribeFilteredPersonViewNotification(Integer start,
                                                                           Integer end,
                                                                           PersonRangeFilterDto filter) {
        return personService.getFilteredPersonViewNotification(start, end, filter);
    }

    @Query
    @Description("Say hello")
    public String sayHello(@DefaultValue("World") String name) {
        return "Hello " + name;
    }
}