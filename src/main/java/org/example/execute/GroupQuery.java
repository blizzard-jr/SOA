package org.example.execute;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Component
public class GroupQuery {
    public record Page(List<StudyGroup> content, long totalElements, int page, int size, int totalPages) {}

    public Page select(List<StudyGroup> source, MultiValueMap<String, String> params) {
        int page = integer(params.getFirst("page"), 0, "page");
        int size = integer(params.getFirst("size"), 20, "size");
        if (page < 0 || size < 1 || size > 1000)
            throw bad("page must be non-negative and size must be between 1 and 1000");
        List<StudyGroup> selected = new ArrayList<>(source);
        for (var entry : params.entrySet()) {
            String key = entry.getKey();
            String field = key.startsWith("filter.") ? key.substring(7) :
                    key.startsWith("filter[") && key.endsWith("]") ? key.substring(7, key.length() - 1) : null;
            if (field == null) {
                if (!Set.of("page", "size", "sort").contains(key)) throw bad("Unknown parameter: " + key);
                continue;
            }
            fieldValue(null, field); // Validate even when the collection is empty.
            if (entry.getValue().size() != 1) throw bad("Filter must have one value: " + key);
            String expected = entry.getValue().getFirst();
            selected.removeIf(group -> !Objects.equals(stringValue(fieldValue(group, field)), expected));
        }
        List<String> sortFields = new ArrayList<>();
        for (String sort : params.getOrDefault("sort", List.of()))
            for (String field : sort.split(",")) if (!field.isBlank()) sortFields.add(field.trim());
        Comparator<StudyGroup> comparator = null;
        for (String expression : sortFields) {
            boolean descending = expression.startsWith("-");
            String field = descending ? expression.substring(1) : expression;
            fieldValue(null, field);
            Comparator<StudyGroup> next = (a, b) -> compare(fieldValue(a, field), fieldValue(b, field));
            if (descending) next = next.reversed();
            comparator = comparator == null ? next : comparator.thenComparing(next);
        }
        if (comparator != null) selected.sort(comparator);
        long total = selected.size();
        long offset = (long) page * size;
        List<StudyGroup> content = offset >= total ? List.of() :
                List.copyOf(selected.subList((int) offset, (int) Math.min(total, offset + size)));
        return new Page(content, total, page, size, (int) ((total + size - 1) / size));
    }

    private static int integer(String value, int fallback, String name) {
        if (value == null) return fallback;
        try { return Integer.parseInt(value); }
        catch (NumberFormatException ex) { throw bad("Invalid " + name); }
    }

    private static String stringValue(Object value) { return value == null ? "null" : value.toString(); }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static int compare(Object a, Object b) {
        if (a == null) return b == null ? 0 : -1;
        if (b == null) return 1;
        return ((Comparable) a).compareTo(b);
    }

    private static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private static Object fieldValue(StudyGroup g, String field) {
        if (g == null) {
            if (!FIELDS.contains(field)) throw bad("Unknown field: " + field);
            return null;
        }
        StudyGroup.Person admin = g.groupAdmin();
        StudyGroup.Location location = admin == null ? null : admin.location();
        return switch (field) {
            case "id" -> g.id();
            case "name" -> g.name();
            case "coordinates.x" -> g.coordinates().x();
            case "coordinates.y" -> g.coordinates().y();
            case "creationDate" -> g.creationDate();
            case "studentsCount" -> g.studentsCount();
            case "expelledStudents" -> g.expelledStudents();
            case "shouldBeExpelled" -> g.shouldBeExpelled();
            case "formOfEducation" -> g.formOfEducation();
            case "groupAdmin.name" -> admin == null ? null : admin.name();
            case "groupAdmin.weight" -> admin == null ? null : admin.weight();
            case "groupAdmin.eyeColor" -> admin == null ? null : admin.eyeColor();
            case "groupAdmin.hairColor" -> admin == null ? null : admin.hairColor();
            case "groupAdmin.nationality" -> admin == null ? null : admin.nationality();
            case "groupAdmin.location.x" -> location == null ? null : location.x();
            case "groupAdmin.location.y" -> location == null ? null : location.y();
            case "groupAdmin.location.z" -> location == null ? null : location.z();
            case "groupAdmin.location.name" -> location == null ? null : location.name();
            default -> throw bad("Unknown field: " + field);
        };
    }

    public static final Set<String> FIELDS = Set.of("id", "name", "coordinates.x", "coordinates.y",
            "creationDate", "studentsCount", "expelledStudents", "shouldBeExpelled", "formOfEducation",
            "groupAdmin.name", "groupAdmin.weight", "groupAdmin.eyeColor", "groupAdmin.hairColor",
            "groupAdmin.nationality", "groupAdmin.location.x", "groupAdmin.location.y",
            "groupAdmin.location.z", "groupAdmin.location.name");
}
