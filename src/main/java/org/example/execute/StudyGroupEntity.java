package org.example.execute;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "study_groups")
class StudyGroupEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private float coordinateX;
    @Column(nullable = false)
    private Float coordinateY;
    @Column(nullable = false, updatable = false)
    private LocalDate creationDate;
    @Column(nullable = false)
    private long studentsCount;
    @Column(nullable = false)
    private Long expelledStudents;
    private Long shouldBeExpelled;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudyGroup.FormOfEducation formOfEducation;

    private String adminName;
    private Integer adminWeight;
    @Enumerated(EnumType.STRING)
    private StudyGroup.EyeColor adminEyeColor;
    @Enumerated(EnumType.STRING)
    private StudyGroup.HairColor adminHairColor;
    @Enumerated(EnumType.STRING)
    private StudyGroup.Country adminNationality;
    private Long adminLocationX;
    private Integer adminLocationY;
    private Long adminLocationZ;
    @Column(length = 792)
    private String adminLocationName;

    protected StudyGroupEntity() {}

    StudyGroupEntity(StudyGroup.Input input) {
        creationDate = LocalDate.now();
        apply(input);
    }

    void apply(StudyGroup.Input input) {
        name = input.name();
        coordinateX = input.coordinates().x();
        coordinateY = input.coordinates().y();
        studentsCount = input.studentsCount();
        expelledStudents = input.expelledStudents();
        shouldBeExpelled = input.shouldBeExpelled();
        formOfEducation = input.formOfEducation();
        StudyGroup.Person admin = input.groupAdmin();
        if (admin == null) {
            adminName = null;
            adminWeight = null;
            adminEyeColor = null;
            adminHairColor = null;
            adminNationality = null;
            adminLocationX = null;
            adminLocationY = null;
            adminLocationZ = null;
            adminLocationName = null;
        } else {
            adminName = admin.name();
            adminWeight = admin.weight();
            adminEyeColor = admin.eyeColor();
            adminHairColor = admin.hairColor();
            adminNationality = admin.nationality();
            adminLocationX = admin.location().x();
            adminLocationY = admin.location().y();
            adminLocationZ = admin.location().z();
            adminLocationName = admin.location().name();
        }
    }

    void changeStudentsCount(long studentsCount) {
        this.studentsCount = studentsCount;
    }

    StudyGroup toModel() {
        StudyGroup.Person admin = adminName == null ? null : new StudyGroup.Person(
                adminName, adminWeight, adminEyeColor, adminHairColor, adminNationality,
                new StudyGroup.Location(adminLocationX, adminLocationY, adminLocationZ, adminLocationName));
        return new StudyGroup(id, name, new StudyGroup.Coordinates(coordinateX, coordinateY), creationDate,
                studentsCount, expelledStudents, shouldBeExpelled, formOfEducation, admin);
    }
}
