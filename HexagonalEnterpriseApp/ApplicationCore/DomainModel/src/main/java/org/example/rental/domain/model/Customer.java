package org.example.rental.domain.model;

public class Customer extends User {
    private String phoneNumber;
    private String address;

    public Customer() {
        super();
    }

    public Customer(String login, String name, String email, String phoneNumber, String address) {
        super(login, name, email);
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    // NIE nadpisuj setPassword() ani getPassword() tutaj!
    // Pozostaw dziedziczenie z User

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}