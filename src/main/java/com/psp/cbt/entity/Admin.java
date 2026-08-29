package com.psp.cbt.entity;

import jakarta.persistence.*;

// Represents one row in the "admins" table.
// @Entity tells Spring/Hibernate: "this class maps to a database table".
@Entity
@Table(name = "admins")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment primary key
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    // This stores a BCrypt HASH of the password, never the plain text.
    @Column(nullable = false)
    private String password;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
