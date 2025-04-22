package com.code.mydiary;

public class User {
    private long id;
    private String email;
    private String password;
    private int sex;
    private long lover;

    public User() {}

    public User(String email, String password, int sex, long lover) {
        this.email = email;
        this.password = password;
        this.sex = sex;
        this.lover = lover;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public long getLover() {
        return lover;
    }

    public void setLover(long lover) {
        this.lover = lover;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", sex=" + sex +
                ", lover=" + lover +
                '}';
    }
}