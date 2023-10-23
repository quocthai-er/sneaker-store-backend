package com.example.sneakerstorebackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    private String id;
    @NotBlank(message = "Name is required")
    @Indexed(unique = true)
    @TextIndexed(weight = 9)
    private String name;
    private String image;
    private boolean root = true;
    private String state;
    @DocumentReference
    @Indexed
    private List<Category> subCategories = new ArrayList<>();


    public Category(String name, String state) {
        this.name = name;
        this.state = state;
    }

}