package com.thilina.WorkingTimeApplication.model;

import com.thilina.WorkingTimeApplication.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role; // PM or ENGINEER

    @OneToMany(mappedBy = "assignedTo", cascade = CascadeType.ALL)
    private List<Task> tasks = new ArrayList<>();
}
