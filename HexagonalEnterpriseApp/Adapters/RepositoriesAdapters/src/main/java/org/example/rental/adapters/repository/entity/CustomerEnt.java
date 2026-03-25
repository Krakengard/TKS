package org.example.rental.adapters.repository.entity;

public class CustomerEnt extends UserEnt {

    private String phoneNumber;
    private String address;

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
