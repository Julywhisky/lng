package cn.edu.hdu.lab505.innovation.domain.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hhx on 2016/11/19.
 */
@Entity
@Table(name = "t_role")
public class Role implements Serializable {
    @Id
    @GeneratedValue
    private int id;
    @Column(nullable = false)
    private String name;
    @ManyToMany(mappedBy = "roleList")
    private List<Account> accountList = new ArrayList<Account>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Account> getAccountList() {
        return accountList;
    }

    public void setAccountList(List<Account> accountList) {
        this.accountList = accountList;
    }
}
