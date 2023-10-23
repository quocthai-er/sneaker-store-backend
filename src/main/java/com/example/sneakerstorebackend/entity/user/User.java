package com.example.sneakerstorebackend.entity.user;

import com.example.sneakerstorebackend.entity.order.Order;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String id;
    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "Email is required")
    @Size(max = 50)
    @Email(message = "Email invalidate")
    @Indexed(unique = true)
    private String email;
    @NotNull(message = "Password can not be null")
    @Size( min = 5, max = 50)
    @JsonIgnore
    private String password;
    private String phone;
    private int province;
    private int district;
    private int ward;
    private String address;
    @NotBlank(message = "Role is required")
    private String role;
    private EGender gender;
    private EProvider provider;
    @NotBlank(message = "State is required")
    private String state;

    @ReadOnlyProperty
    @DocumentReference(lookup="{'user':?#{#self._id} }", lazy = true)
    @JsonIgnore
    @Indexed
    private List<Order> orders;

   /* @ReadOnlyProperty
    @DocumentReference(lookup="{'user':?#{#self._id} }", lazy = true)
    @JsonIgnore
    @Indexed
    private List<Review> reviews;*/

    @JsonIgnore
    private Token token;

    @JsonIgnore
    @Indexed
    private Map<Object, Integer> recommendRanking = new HashMap<>();
    @CreatedDate
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    LocalDateTime createdDate;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    @LastModifiedDate
    LocalDateTime lastModifiedDate;

    public User(String name, String email, String password, String phone, Integer province, Integer district, Integer ward, String address, String role, EGender gender, String state, EProvider provider) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.province = province;
        this.district = district;
        this.ward = ward;
        this.address = address;
        this.role = role;
        this.gender = gender;
        this.state = state;
        this.provider = provider;
    }
}
