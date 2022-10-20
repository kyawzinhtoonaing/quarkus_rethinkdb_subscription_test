package org.acme.model.dto;

import org.eclipse.microprofile.graphql.NonNull;

public class CreatePersonDto {
    @NonNull private String name;
    @NonNull private int age;

    public CreatePersonDto() {
    }

    public CreatePersonDto(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
