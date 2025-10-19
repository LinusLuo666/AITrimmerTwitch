package com.example.aitrimmertwitch.repository;

import com.example.aitrimmertwitch.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
