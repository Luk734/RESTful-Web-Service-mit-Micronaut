package de.dhbw.vs;

import de.dhbw.vs.studentenverwaltung.model.Student;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import java.util.concurrent.ThreadLocalRandom;

@MicronautTest
@Property(name = "micronaut.server.port", value = "-1")
class StudentenverwaltungTest {

    @Inject
    EmbeddedApplication<?> application;

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void testCrudLifecycle() {
        Assertions.assertTrue(application.isRunning());

        String matrikelnummer = uniqueMatrikelnummer();
        Student payload = new Student(null, "Max", "Mustermann", matrikelnummer, "Wirtschaftsinformatik", 4);

        Student created = client.toBlocking().retrieve(HttpRequest.POST("/studenten", payload), Student.class);
        Assertions.assertNotNull(created.getId());
        Assertions.assertEquals(matrikelnummer, created.getMatrikelnummer());

        Student fetched = client.toBlocking().retrieve(HttpRequest.GET("/studenten/" + created.getId()), Student.class);
        Assertions.assertEquals(created.getId(), fetched.getId());
        Assertions.assertEquals("Max", fetched.getVorname());

        Student updatePayload = new Student(null, "Max", "Mustermann", matrikelnummer, "Informatik", 5);
        Student updated = client.toBlocking().retrieve(HttpRequest.PUT("/studenten/" + created.getId(), updatePayload), Student.class);
        Assertions.assertEquals(created.getId(), updated.getId());
        Assertions.assertEquals("Informatik", updated.getStudiengang());
        Assertions.assertEquals(5, updated.getSemester());

        client.toBlocking().exchange(HttpRequest.DELETE("/studenten/" + created.getId()));

        HttpClientResponseException notFoundException = Assertions.assertThrows(
                HttpClientResponseException.class,
                () -> client.toBlocking().exchange(HttpRequest.GET("/studenten/" + created.getId()), Student.class)
        );
        Assertions.assertEquals(HttpStatus.NOT_FOUND, notFoundException.getStatus());
    }

    @Test
    void testValidationAndDuplicateMatrikelnummer() {
        Student invalidPayload = new Student(null, "", "", "12ab", "", 0);

        HttpClientResponseException badRequestException = Assertions.assertThrows(
                HttpClientResponseException.class,
                () -> client.toBlocking().exchange(HttpRequest.POST("/studenten", invalidPayload), Student.class)
        );
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, badRequestException.getStatus());

        String duplicateMatrikelnummer = uniqueMatrikelnummer();
        Student first = new Student(null, "Anna", "Meyer", duplicateMatrikelnummer, "Informatik", 2);
        Student second = new Student(null, "Ben", "Schmitt", duplicateMatrikelnummer, "Informatik", 2);

        client.toBlocking().exchange(HttpRequest.POST("/studenten", first), Student.class);

        HttpClientResponseException conflictException = Assertions.assertThrows(
                HttpClientResponseException.class,
                () -> client.toBlocking().exchange(HttpRequest.POST("/studenten", second), Student.class)
        );
        Assertions.assertEquals(HttpStatus.CONFLICT, conflictException.getStatus());
    }

    private String uniqueMatrikelnummer() {
        int value = ThreadLocalRandom.current().nextInt(1_000_000, 10_000_000);
        return Integer.toString(value);
    }
}
