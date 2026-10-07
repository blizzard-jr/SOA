package org.example.execute;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/isu")
@Tag(name = "ISU", description = "Дополнительные операции, вызывающие HTTP API сервиса групп")
public class IsuController {
    private final RestClient client;

    public IsuController(RestClient.Builder builder, @Value("${app.groups-base-url:http://localhost:8080}") String baseUrl) {
        this.client = builder.baseUrl(baseUrl).build();
    }

    public record TotalExpelled(long totalExpelled) {}

    @GetMapping("/statistics/count-expelled")
    @Operation(summary = "Общее число отчисленных студентов", description = "Суммирует expelledStudents всех групп, запрашивая страницы первого сервиса.")
    public TotalExpelled countExpelled() {
        long total = 0;
        try {
            for (int page = 0; ; page++) {
                GroupQuery.Page response = client.get().uri("/groups?page={page}&size=1000", page)
                        .retrieve().body(GroupQuery.Page.class);
                if (response == null) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Empty response from groups service");
                for (StudyGroup group : response.content()) {
                    try { total = Math.addExact(total, group.expelledStudents()); }
                    catch (ArithmeticException ex) { throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Sum overflow"); }
                }
                if (page + 1 >= response.totalPages()) return new TotalExpelled(total);
            }
        } catch (ResourceAccessException | RestClientResponseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Groups service unavailable", ex);
        }
    }

    @PostMapping("/group/{group-id}/add-student")
    @Operation(summary = "Добавить студента в группу", description = "Вызывает GET и PUT первого сервиса; увеличивает studentsCount на единицу.")
    public StudyGroup addStudent(@PathVariable("group-id") @Positive int id) {
        try {
            StudyGroup group = client.get().uri("/groups/{id}", id).retrieve().body(StudyGroup.class);
            if (group == null) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Empty response from groups service");
            if (group.studentsCount() == Long.MAX_VALUE)
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "studentsCount overflow");
            StudyGroup updated = group.withStudentsCount(group.studentsCount() + 1);
            StudyGroup result = client.put().uri("/groups/{id}", id).body(updated.input()).retrieve().body(StudyGroup.class);
            if (result == null) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Empty response from groups service");
            return result;
        } catch (RestClientResponseException ex) {
            throw new ResponseStatusException(HttpStatus.valueOf(ex.getStatusCode().value()), "Groups service returned " + ex.getStatusCode().value(), ex);
        } catch (ResourceAccessException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Groups service unavailable", ex);
        }
    }
}
