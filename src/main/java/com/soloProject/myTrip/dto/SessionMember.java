package com.soloProject.myTrip.dto;

import com.soloProject.myTrip.entity.Member;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class SessionMember implements Serializable {
    private String name;
    private String email;

    public SessionMember(Member member){
        this.name = member.getName();
        this.email = member.getEmail();
    }
}
