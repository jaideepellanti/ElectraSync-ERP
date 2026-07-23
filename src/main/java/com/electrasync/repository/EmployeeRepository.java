package com.electrasync.repository;

import com.electrasync.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByStatus(Employee.EmployeeStatus status);

    // Used to check for duplicate phone numbers before saving an employee
    Optional<Employee> findByPhone(String phone);
}
