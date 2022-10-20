package org.acme.service;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.gen.ast.Filter;
import com.rethinkdb.gen.ast.ReqlExpr;
import com.rethinkdb.gen.ast.Table;
import com.rethinkdb.net.Result;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import org.acme.config.db.RethinkDBConnectionFactory;
import org.acme.model.dto.CreatePersonDto;
import org.acme.model.dto.PersonRangeFilterDto;
import org.acme.model.viewonly.PersonView;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Startup
@ApplicationScoped
public class PersonService {
    private static final RethinkDB r = RethinkDB.r;
    private final RethinkDBConnectionFactory connectionFactory;
    private Multi<PersonView> personViewStream;

    public PersonService(RethinkDBConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @PostConstruct
    public void init() {
        Result<PersonView> result = r.table(RethinkDBConnectionFactory.TABLE_PERSON_VIEWONLY).changes()
                .getField("new_val")
                .run(connectionFactory.getConnection(), PersonView.class);

        personViewStream = Multi.createFrom().iterable(result)
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    public void createPersonView(CreatePersonDto createPersonDto) {
        r.table(RethinkDBConnectionFactory.TABLE_PERSON_VIEWONLY).insert(
                r.hashMap("name", createPersonDto.getName())
                        .with("age", createPersonDto.getAge())
        ).run(connectionFactory.getConnection());
    }

    public Multi<PersonView> getPersonViewNotification() {
        return personViewStream;
    }

    public Multi<List<PersonView>> getFilteredPersonViewNotification(Integer start, Integer end, PersonRangeFilterDto filter) {
        return personViewStream
                .map(personView -> {
                    return retrieveFilteredPersonViews(start, end, filter);
                })
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    public List<PersonView> retrieveFilteredPersonViews(Integer start, Integer end, PersonRangeFilterDto filter) {
        final var personTable = RethinkDBConnectionFactory.TABLE_PERSON_VIEWONLY;

        ReqlExpr reqlExpr = r.table(personTable).filter(row -> {
            ReqlExpr filterExpr = row;

            if (filter != null) {
                if (filter.getAge() != null) {
                    filterExpr = filterExpr.and(filterExpr.g("age").eq(filter.getAge()));
                }

                if (filter.getName() != null && !filter.getName().isBlank()) {
                    filterExpr = filterExpr.and(filterExpr.g("name").eq(filter.getName()));
                }
            }

            return filterExpr;
        });

        if (start != null && end != null) {
            reqlExpr = reqlExpr.slice(start, end + 1);
        } else if (start != null) {
            reqlExpr = reqlExpr.slice(start);
        }

        List<PersonView> data = reqlExpr.run(connectionFactory.getConnection(), PersonView.class)
                .collect(Collectors.toList());

        if (data == null) {
            data = new ArrayList<>();
        }

        return data;
    }
}
