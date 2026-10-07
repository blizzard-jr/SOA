package org.example.execute;

import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GroupRepository {
    private final StudyGroupJpaRepository database;

    public GroupRepository(StudyGroupJpaRepository database) {
        this.database = database;
    }

    @Transactional
    public StudyGroup create(StudyGroup.Input input) {
        return database.saveAndFlush(new StudyGroupEntity(input)).toModel();
    }

    public StudyGroup get(int id) {
        return entity(id).toModel();
    }

    @Transactional
    public StudyGroup update(int id, StudyGroup.Input input) {
        StudyGroupEntity entity = entity(id);
        entity.apply(input);
        return database.save(entity).toModel();
    }

    @Transactional
    public StudyGroup patchStudentsCount(int id, long studentsCount) {
        StudyGroupEntity entity = entity(id);
        entity.changeStudentsCount(studentsCount);
        return database.save(entity).toModel();
    }

    @Transactional
    public void delete(int id) {
        StudyGroupEntity entity = entity(id);
        database.delete(entity);
    }

    public List<StudyGroup> all() {
        return database.findAll(Sort.by("id")).stream().map(StudyGroupEntity::toModel).toList();
    }

    public StudyGroup minId() {
        return database.findFirstByOrderByIdAsc().map(StudyGroupEntity::toModel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection is empty"));
    }

    private StudyGroupEntity entity(int id) {
        return database.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));
    }
}
