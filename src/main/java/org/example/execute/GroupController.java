package org.example.execute;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/groups")
@Tag(name = "Study groups", description = "Управление коллекцией учебных групп")
public class GroupController {
    private final GroupRepository repository;
    private final GroupQuery query;

    public GroupController(GroupRepository repository, GroupQuery query) {
        this.repository = repository;
        this.query = query;
    }

    @GetMapping
    public GroupQuery.Page list(@Parameter(hidden = true) @RequestParam MultiValueMap<String, String> params) {
        return query.select(repository.all(), params);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать группу", description = "ID и дата создания назначаются сервером. Передайте остальные поля в JSON.")
    public ResponseEntity<StudyGroup> create(@Valid @RequestBody StudyGroup.Input input) {
        StudyGroup group = repository.create(input);
        return ResponseEntity.created(URI.create("/groups/" + group.id())).body(group);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить группу по ID")
    public StudyGroup get(@PathVariable @Positive int id) { return repository.get(id); }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить группу", description = "Полная замена изменяемых полей; ID и дата создания сохраняются.")
    public StudyGroup update(@PathVariable @Positive int id, @Valid @RequestBody StudyGroup.Input input) {
        return repository.update(id, input);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Частично обновить число студентов",
            description = "Изменяет только studentsCount; остальные поля группы сохраняются.")
    public StudyGroup patchStudentsCount(@PathVariable @Positive int id,
                                         @Valid @RequestBody StudyGroup.StudentsCountPatch patch) {
        return repository.patchStudentsCount(id, patch.studentsCount());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить группу")
    public void delete(@PathVariable @Positive int id) { repository.delete(id); }

    public record Average(double average) {}

    @GetMapping("/statistics/average-expelled")
    @Operation(summary = "Среднее число отчисленных студентов", description = "Для пустой коллекции возвращает 404.")
    public Average averageExpelled() {
        List<StudyGroup> all = repository.all();
        if (all.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Collection is empty");
        return new Average(all.stream().mapToLong(StudyGroup::expelledStudents).average().orElseThrow());
    }

    @GetMapping("/min-id")
    @Operation(summary = "Группа с минимальным ID", description = "Для пустой коллекции возвращает 404.")
    public StudyGroup minId() { return repository.minId(); }

    public record DateCount(LocalDate creationDate, long count) {}

    @GetMapping("/statistics/count-by-creation-date")
    @Operation(summary = "Количество групп по датам создания")
    public List<DateCount> countByDate() {
        Map<LocalDate, Long> counts = new TreeMap<>();
        repository.all().forEach(group -> counts.merge(group.creationDate(), 1L, Long::sum));
        return counts.entrySet().stream().map(e -> new DateCount(e.getKey(), e.getValue())).toList();
    }
}
