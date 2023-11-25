package com.mycompany.telegramsimplebot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;



// base of advertisement texts
@Getter
@Setter
@Entity(name = "adstable")
public class Ads {
    
    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @Column(name="ad")
    private String ad;

    @Override
    public String toString() {
        return "Ads{" + "id=" + id + ", ad=" + ad + '}';
    }
    
    
}
