package org.example.execute;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record StudyGroup(
        @Positive int id,
        @NotBlank String name,
        @NotNull @Valid Coordinates coordinates,
        @NotNull LocalDate creationDate,
        @Positive long studentsCount,
        @NotNull @Positive Long expelledStudents,
        @Positive Long shouldBeExpelled,
        @NotNull FormOfEducation formOfEducation,
        @Valid Person groupAdmin
) {
    public record Input(
            @NotBlank String name,
            @NotNull @Valid Coordinates coordinates,
            @Positive long studentsCount,
            @NotNull @Positive Long expelledStudents,
            @Positive Long shouldBeExpelled,
            @NotNull FormOfEducation formOfEducation,
            @Valid Person groupAdmin
    ) {}

    public record StudentsCountPatch(@NotNull @Positive Long studentsCount) {}

    public record Coordinates(float x, @NotNull Float y) {}

    public record Person(
            @NotBlank String name,
            @Positive int weight,
            EyeColor eyeColor,
            HairColor hairColor,
            Country nationality,
            @NotNull @Valid Location location
    ) {}

    public record Location(long x, int y, long z, @NotNull @Size(max = 792) String name) {}

    public enum FormOfEducation { DISTANCE_EDUCATION, FULL_TIME_EDUCATION, EVENING_CLASSES }
    public enum EyeColor { GREEN, RED, BROWN }
    public enum HairColor { GREEN, RED, ORANGE, BROWN }
    public enum Country { UNITED_KINGDOM, INDIA, VATICAN, SOUTH_KOREA }

    public Input input() {
        return new Input(name, coordinates, studentsCount, expelledStudents,
                shouldBeExpelled, formOfEducation, groupAdmin);
    }

    public StudyGroup withStudentsCount(long count) {
        return new StudyGroup(id, name, coordinates, creationDate, count,
                expelledStudents, shouldBeExpelled, formOfEducation, groupAdmin);
    }
}
