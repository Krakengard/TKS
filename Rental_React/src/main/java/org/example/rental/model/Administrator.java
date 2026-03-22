package org.example.rental.model;

public class Administrator extends User {
    private String department;

    public Administrator() {
        super();
    }

    public Administrator(String login, String name, String email, String department) {
        super(login, name, email);
        this.department = department;
    }

    // NIE nadpisuj setPassword() ani getPassword() tutaj!

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}