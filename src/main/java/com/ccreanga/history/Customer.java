package com.ccreanga.history;

import java.util.Arrays;
import java.util.Objects;
import java.util.TreeSet;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.util.List;

@Getter
@Setter
public class Customer{
   private String name;
   private long[] matches;
   private int hashCode;

   public Customer(String name, long[] matches) {
       this.name = name;
       this.matches = matches;
       hashCode = Objects.hash(name);
   }

   public boolean hasMatch(long match){
       if (matches.length==1){
           return matches[0]==match;
       }
       return Arrays.binarySearch(matches,match)!=-1;
   }

   @Override
   public boolean equals(Object o) {
       if (this == o) {
           return true;
       }
       if (o == null || getClass() != o.getClass()) {
           return false;
       }
       Customer customer = (Customer) o;
       return name.equals(customer.name);
   }

   @Override
   public int hashCode() {
       return hashCode;
   }
}
