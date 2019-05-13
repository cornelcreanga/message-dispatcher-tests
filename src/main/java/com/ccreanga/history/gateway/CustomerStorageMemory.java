package com.ccreanga.history.gateway;

import java.util.HashSet;

import com.ccreanga.history.Customer;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Set;

@Component
public class CustomerStorageMemory implements CustomerStorage {

    private Set<Customer> set = new HashSet<>();

    @Override
    public void addCustomer(Customer customer) {
        set.add(customer);
    }

    @Override
    public void removeCustomer(Customer customer) {
        set.remove(customer);
    }

    @Override
    public Set<Customer> getCustomers() {
        return Collections.unmodifiableSet(set);
    }

    @PostConstruct
    private void init(){
        set.add(new Customer("test1", new long[]{1,2}));
        set.add(new Customer("test2", new long[]{1,2,3,4,5}));
        set.add(new Customer("test3", new long[]{1}));
        set.add(new Customer("test4", new long[]{1,2,3}));
        set.add(new Customer("test5", new long[]{1,2,3,4,5}));
    }


}
