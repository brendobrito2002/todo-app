package com.myapp.todoapp.specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.myapp.todoapp.model.entity.Task;
import com.myapp.todoapp.model.enums.Priority;
import com.myapp.todoapp.model.enums.Status;

public class TaskSpecification {
    public static Specification<Task> filter(
            Long userId,
            Status status,
            Priority priority,
            LocalDate dueDate
    ) {
        return (root, query, cb) -> {
        	List<Predicate> predicates = new ArrayList<>();
        	predicates.add(cb.equal(root.get("user").get("id"), userId));

        	if (status != null) {
        	    predicates.add(cb.equal(root.get("status"), status));
        	}

        	if (priority != null) {
        	    predicates.add(cb.equal(root.get("priority"), priority));
        	}

        	if (dueDate != null) {
        	    predicates.add(cb.equal(root.get("dueDate"), dueDate));
        	}

        	return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
